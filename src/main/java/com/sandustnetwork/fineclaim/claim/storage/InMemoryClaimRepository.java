package com.sandustnetwork.fineclaim.claim.storage;

import com.sandustnetwork.fineclaim.claim.domain.Claim;
import com.sandustnetwork.fineclaim.claim.domain.ClaimChunk;
import com.sandustnetwork.fineclaim.claim.domain.ClaimId;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryClaimRepository implements ClaimRepository {

    private final Map<ClaimId, Claim> claimsById = new ConcurrentHashMap<>();
    private final Map<ClaimChunk, ClaimId> claimIndex = new ConcurrentHashMap<>();

    @Override
    public Collection<Claim> findAllClaims() {
        return List.copyOf(claimsById.values());
    }

    @Override
    public Optional<Claim> findByChunk(ClaimChunk chunk) {
        Objects.requireNonNull(chunk, "chunk");
        ClaimId claimId = claimIndex.get(chunk);
        if (claimId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(claimsById.get(claimId));
    }

    @Override
    public Optional<Claim> findById(ClaimId claimId) {
        Objects.requireNonNull(claimId, "claimId");
        return Optional.ofNullable(claimsById.get(claimId));
    }

    @Override
    public void save(Claim claim) {
        Objects.requireNonNull(claim, "claim");

        Claim previousClaim = claimsById.get(claim.getId());
        if (previousClaim != null) {
            removeFromIndex(previousClaim);
        }

        claimsById.put(claim.getId(), claim);
        addToIndex(claim);
    }

    @Override
    public void delete(ClaimId claimId) {
        Objects.requireNonNull(claimId, "claimId");
        Claim removedClaim = claimsById.remove(claimId);
        if (removedClaim != null) {
            removeFromIndex(removedClaim);
        }
    }

    @Override
    public int countChunksByOwner(UUID owner) {
        Objects.requireNonNull(owner, "owner");
        int total = 0;
        for (Claim claim : claimsById.values()) {
            if (claim.getOwner().equals(owner)) {
                total += claim.chunkCount();
            }
        }
        return total;
    }

    @Override
    public int countTotalChunks() {
        int total = 0;
        for (Claim claim : claimsById.values()) {
            total += claim.chunkCount();
        }
        return total;
    }

    private void addToIndex(Claim claim) {
        for (ClaimChunk chunk : claim.getChunks()) {
            claimIndex.put(chunk, claim.getId());
        }
    }

    private void removeFromIndex(Claim claim) {
        for (ClaimChunk chunk : claim.getChunks()) {
            claimIndex.remove(chunk);
        }
    }
}
