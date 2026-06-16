package com.sandustnetwork.fineclaim.claim.application;

import com.sandustnetwork.fineclaim.claim.config.ClaimLimitChecker;
import com.sandustnetwork.fineclaim.claim.domain.Claim;
import com.sandustnetwork.fineclaim.claim.domain.ClaimChunk;
import com.sandustnetwork.fineclaim.claim.domain.ClaimChunkAdjacency;
import com.sandustnetwork.fineclaim.claim.domain.ClaimId;
import com.sandustnetwork.fineclaim.claim.storage.ClaimRepository;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class ClaimService {

    private final ClaimRepository repository;
    private final ClaimLimitChecker limitChecker;

    public ClaimService(ClaimRepository repository, ClaimLimitChecker limitChecker) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.limitChecker = Objects.requireNonNull(limitChecker, "limitChecker");
    }

    public ClaimOperationResult createRegion(ClaimChunk chunk, UUID owner) {
        Objects.requireNonNull(chunk, "chunk");
        Objects.requireNonNull(owner, "owner");

        if (repository.findByChunk(chunk).isPresent()) {
            return ClaimOperationResult.failure("This chunk is already claimed.");
        }
        if (!limitChecker.canAddChunk(owner)) {
            return ClaimOperationResult.failure(limitChecker.limitFailureMessage(owner));
        }

        Claim claim = new Claim(
                ClaimId.random(),
                Set.of(chunk),
                owner,
                Instant.now(),
                Set.of()
        );
        repository.save(claim);
        return ClaimOperationResult.success("Claim created.");
    }

    public ClaimOperationResult expandRegion(ClaimChunk targetChunk, UUID owner) {
        Objects.requireNonNull(targetChunk, "targetChunk");
        Objects.requireNonNull(owner, "owner");

        if (repository.findByChunk(targetChunk).isPresent()) {
            return ClaimOperationResult.failure("This chunk is already claimed.");
        }
        if (!limitChecker.canAddChunk(owner)) {
            return ClaimOperationResult.failure(limitChecker.limitFailureMessage(owner));
        }

        Optional<Claim> adjacentRegion = findAdjacentOwnedRegion(targetChunk, owner);
        if (adjacentRegion.isEmpty()) {
            return ClaimOperationResult.failure("You must stand in an unclaimed chunk adjacent to your claim.");
        }

        Claim claim = adjacentRegion.get();
        Claim updatedClaim = claim.withChunkAdded(targetChunk);
        repository.save(updatedClaim);
        return ClaimOperationResult.success("Claim expanded.");
    }

    public ClaimOperationResult shrinkRegion(ClaimChunk targetChunk, UUID owner) {
        Objects.requireNonNull(targetChunk, "targetChunk");
        Objects.requireNonNull(owner, "owner");

        Optional<Claim> existingClaim = repository.findByChunk(targetChunk);
        if (existingClaim.isEmpty()) {
            return ClaimOperationResult.failure("No claim exists at this chunk.");
        }

        Claim claim = existingClaim.get();
        if (!claim.isOwner(owner)) {
            return ClaimOperationResult.failure("Only the claim owner can shrink this claim.");
        }
        if (!claim.containsChunk(targetChunk)) {
            return ClaimOperationResult.failure("This chunk is not part of your claim.");
        }
        if (!ClaimChunkAdjacency.isEdgeChunk(targetChunk, claim.getChunks())) {
            return ClaimOperationResult.failure("You can only shrink edge chunks of your claim.");
        }

        if (claim.chunkCount() == 1) {
            repository.delete(claim.getId());
            return ClaimOperationResult.success("Claim removed.");
        }

        Claim updatedClaim = claim.withChunkRemoved(targetChunk);
        repository.save(updatedClaim);
        return ClaimOperationResult.success("Claim shrunk.");
    }

    public ClaimOperationResult deleteRegionAt(ClaimChunk chunk, UUID requester) {
        Objects.requireNonNull(chunk, "chunk");
        Objects.requireNonNull(requester, "requester");

        Optional<Claim> existingClaim = repository.findByChunk(chunk);
        if (existingClaim.isEmpty()) {
            return ClaimOperationResult.failure("No claim exists at this chunk.");
        }

        Claim claim = existingClaim.get();
        if (!claim.isOwner(requester)) {
            return ClaimOperationResult.failure("Only the claim owner can delete this claim.");
        }

        repository.delete(claim.getId());
        return ClaimOperationResult.success("Claim deleted.");
    }

    public Optional<Claim> getClaimAt(ClaimChunk chunk) {
        Objects.requireNonNull(chunk, "chunk");
        return repository.findByChunk(chunk);
    }

    public boolean canBuild(ClaimChunk chunk, UUID playerId) {
        Objects.requireNonNull(chunk, "chunk");
        Objects.requireNonNull(playerId, "playerId");

        return repository.findByChunk(chunk)
                .map(claim -> claim.canBuild(playerId))
                .orElse(true);
    }

    public boolean isOwner(ClaimChunk chunk, UUID playerId) {
        Objects.requireNonNull(chunk, "chunk");
        Objects.requireNonNull(playerId, "playerId");

        return repository.findByChunk(chunk)
                .map(claim -> claim.isOwner(playerId))
                .orElse(false);
    }

    public boolean isTrusted(ClaimChunk chunk, UUID playerId) {
        Objects.requireNonNull(chunk, "chunk");
        Objects.requireNonNull(playerId, "playerId");

        return repository.findByChunk(chunk)
                .map(claim -> claim.isTrusted(playerId))
                .orElse(false);
    }

    public ClaimOperationResult trustPlayer(ClaimChunk chunk, UUID owner, UUID target) {
        Objects.requireNonNull(chunk, "chunk");
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(target, "target");

        Optional<Claim> existingClaim = repository.findByChunk(chunk);
        if (existingClaim.isEmpty()) {
            return ClaimOperationResult.failure("No claim exists at this chunk.");
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

    public ClaimOperationResult untrustPlayer(ClaimChunk chunk, UUID owner, UUID target) {
        Objects.requireNonNull(chunk, "chunk");
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(target, "target");

        Optional<Claim> existingClaim = repository.findByChunk(chunk);
        if (existingClaim.isEmpty()) {
            return ClaimOperationResult.failure("No claim exists at this chunk.");
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

    private Optional<Claim> findAdjacentOwnedRegion(ClaimChunk targetChunk, UUID owner) {
        Set<ClaimChunk> ownedChunks = new HashSet<>();
        for (Claim claim : repository.findAllClaims()) {
            if (claim.isOwner(owner)) {
                ownedChunks.addAll(claim.getChunks());
            }
        }

        Optional<ClaimChunk> adjacentOwnedChunk = ClaimChunkAdjacency.findAdjacentOwnedChunk(targetChunk, ownedChunks);
        if (adjacentOwnedChunk.isEmpty()) {
            return Optional.empty();
        }

        return repository.findByChunk(adjacentOwnedChunk.get());
    }
}
