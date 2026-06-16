package com.sandustnetwork.fineclaim.claim.application;

import com.sandustnetwork.fineclaim.claim.domain.Claim;
import com.sandustnetwork.fineclaim.claim.domain.ClaimChunk;
import com.sandustnetwork.fineclaim.claim.domain.ClaimId;
import com.sandustnetwork.fineclaim.claim.storage.ClaimRepository;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class ClaimService {

    private final ClaimRepository repository;

    public ClaimService(ClaimRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public ClaimOperationResult createClaim(ClaimChunk chunk, UUID owner) {
        Objects.requireNonNull(chunk, "chunk");
        Objects.requireNonNull(owner, "owner");

        if (repository.findByChunk(chunk).isPresent()) {
            return ClaimOperationResult.failure("This chunk is already claimed.");
        }

        Claim claim = new Claim(
                ClaimId.random(),
                chunk,
                owner,
                Instant.now(),
                Set.of()
        );
        repository.save(claim);
        return ClaimOperationResult.success("Claim created.");
    }

    public ClaimOperationResult deleteClaim(ClaimChunk chunk, UUID requester) {
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

        repository.deleteByChunk(chunk);
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
}
