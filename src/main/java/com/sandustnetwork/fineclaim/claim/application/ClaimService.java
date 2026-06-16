package com.sandustnetwork.fineclaim.claim.application;

import com.sandustnetwork.fineclaim.claim.config.ClaimLimitChecker;
import com.sandustnetwork.fineclaim.claim.domain.Claim;
import com.sandustnetwork.fineclaim.claim.domain.ClaimBox;
import com.sandustnetwork.fineclaim.claim.domain.ClaimId;
import com.sandustnetwork.fineclaim.claim.storage.ClaimRepository;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class ClaimService {

    private final ClaimRepository repository;
    private final ClaimLimitChecker limitChecker;

    public ClaimService(ClaimRepository repository, ClaimLimitChecker limitChecker) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.limitChecker = Objects.requireNonNull(limitChecker, "limitChecker");
    }

    public ClaimOperationResult createFromBox(ClaimBox box, UUID owner) {
        Objects.requireNonNull(box, "box");
        Objects.requireNonNull(owner, "owner");

        String volumeError = limitChecker.validateBox(box.volume());
        if (volumeError != null) {
            return ClaimOperationResult.failure(volumeError);
        }
        if (!repository.findOverlapping(box, null).isEmpty()) {
            return ClaimOperationResult.failure("This area overlaps an existing claim.");
        }
        if (!limitChecker.canAddBlocks(owner, box.volume())) {
            return ClaimOperationResult.failure(limitChecker.limitFailureMessage(owner, box.volume()));
        }

        Claim claim = new Claim(
                ClaimId.random(),
                box,
                owner,
                Instant.now(),
                java.util.Set.of()
        );
        repository.save(claim);
        return ClaimOperationResult.success("Claim created.");
    }

    public ClaimOperationResult resizeClaim(ClaimId claimId, ClaimBox newBox, UUID owner) {
        Objects.requireNonNull(claimId, "claimId");
        Objects.requireNonNull(newBox, "newBox");
        Objects.requireNonNull(owner, "owner");

        Optional<Claim> existingClaim = repository.findById(claimId);
        if (existingClaim.isEmpty()) {
            return ClaimOperationResult.failure("No claim exists with that id.");
        }

        Claim claim = existingClaim.get();
        if (!claim.isOwner(owner)) {
            return ClaimOperationResult.failure("Only the claim owner can resize this claim.");
        }

        String volumeError = limitChecker.validateBox(newBox.volume());
        if (volumeError != null) {
            return ClaimOperationResult.failure(volumeError);
        }

        int blockDelta = newBox.volume() - claim.blockCount();
        if (!limitChecker.canChangeBlocks(owner, blockDelta)) {
            return ClaimOperationResult.failure(limitChecker.limitFailureMessage(owner, blockDelta));
        }

        List<Claim> overlapping = repository.findOverlapping(newBox, claimId);
        if (!overlapping.isEmpty()) {
            return ClaimOperationResult.failure("This area overlaps an existing claim.");
        }

        repository.save(claim.withBox(newBox));
        return ClaimOperationResult.success("Claim resized.");
    }

    public ClaimOperationResult deleteAtBlock(String worldName, int x, int y, int z, UUID requester) {
        Objects.requireNonNull(worldName, "worldName");
        Objects.requireNonNull(requester, "requester");

        Optional<Claim> existingClaim = repository.findByBlock(worldName, x, y, z);
        if (existingClaim.isEmpty()) {
            return ClaimOperationResult.failure("No claim exists at this location.");
        }

        Claim claim = existingClaim.get();
        if (!claim.isOwner(requester)) {
            return ClaimOperationResult.failure("Only the claim owner can delete this claim.");
        }

        repository.delete(claim.getId());
        return ClaimOperationResult.success("Claim deleted.");
    }

    public Optional<Claim> getClaimAtBlock(String worldName, int x, int y, int z) {
        Objects.requireNonNull(worldName, "worldName");
        return repository.findByBlock(worldName, x, y, z);
    }

    public boolean canBuild(String worldName, int x, int y, int z, UUID playerId) {
        Objects.requireNonNull(worldName, "worldName");
        Objects.requireNonNull(playerId, "playerId");

        return repository.findByBlock(worldName, x, y, z)
                .map(claim -> claim.canBuild(playerId))
                .orElse(true);
    }

    public boolean isOwner(String worldName, int x, int y, int z, UUID playerId) {
        Objects.requireNonNull(worldName, "worldName");
        Objects.requireNonNull(playerId, "playerId");

        return repository.findByBlock(worldName, x, y, z)
                .map(claim -> claim.isOwner(playerId))
                .orElse(false);
    }

    public boolean isTrusted(String worldName, int x, int y, int z, UUID playerId) {
        Objects.requireNonNull(worldName, "worldName");
        Objects.requireNonNull(playerId, "playerId");

        return repository.findByBlock(worldName, x, y, z)
                .map(claim -> claim.isTrusted(playerId))
                .orElse(false);
    }

    public ClaimOperationResult trustPlayer(String worldName, int x, int y, int z, UUID owner, UUID target) {
        Objects.requireNonNull(worldName, "worldName");
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(target, "target");

        Optional<Claim> existingClaim = repository.findByBlock(worldName, x, y, z);
        if (existingClaim.isEmpty()) {
            return ClaimOperationResult.failure("No claim exists at this location.");
        }

        Claim claim = existingClaim.get();
        if (!claim.isOwner(owner)) {
            return ClaimOperationResult.failure("Only the claim owner can trust players.");
        }
        if (claim.isOwner(target)) {
            return ClaimOperationResult.failure("The claim owner cannot be trusted.");
        }

        Claim updatedClaim = claim.trust(target);
        if (updatedClaim == claim) {
            return ClaimOperationResult.failure("This player is already trusted.");
        }

        repository.save(updatedClaim);
        return ClaimOperationResult.success("Player trusted.");
    }

    public ClaimOperationResult untrustPlayer(String worldName, int x, int y, int z, UUID owner, UUID target) {
        Objects.requireNonNull(worldName, "worldName");
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(target, "target");

        Optional<Claim> existingClaim = repository.findByBlock(worldName, x, y, z);
        if (existingClaim.isEmpty()) {
            return ClaimOperationResult.failure("No claim exists at this location.");
        }

        Claim claim = existingClaim.get();
        if (!claim.isOwner(owner)) {
            return ClaimOperationResult.failure("Only the claim owner can untrust players.");
        }
        if (claim.isOwner(target)) {
            return ClaimOperationResult.failure("The claim owner cannot be untrusted.");
        }
        if (!claim.isTrusted(target)) {
            return ClaimOperationResult.failure("This player is not trusted.");
        }

        Claim updatedClaim = claim.untrust(target);
        repository.save(updatedClaim);
        return ClaimOperationResult.success("Player untrusted.");
    }
}
