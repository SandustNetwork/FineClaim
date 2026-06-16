package com.sandustnetwork.fineclaim.claim.storage;

import com.sandustnetwork.fineclaim.claim.domain.Claim;
import com.sandustnetwork.fineclaim.claim.domain.ClaimBox;
import com.sandustnetwork.fineclaim.claim.domain.ClaimId;
import com.sandustnetwork.fineclaim.claim.domain.ChunkKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryClaimRepository implements ClaimRepository {

    private final Map<ClaimId, Claim> claimsById = new ConcurrentHashMap<>();
    private final Map<ChunkKey, Set<ClaimId>> chunkIndex = new ConcurrentHashMap<>();

    @Override
    public Collection<Claim> findAllClaims() {
        return List.copyOf(claimsById.values());
    }

    @Override
    public Optional<Claim> findByBlock(String worldName, int x, int y, int z) {
        Objects.requireNonNull(worldName, "worldName");
        ChunkKey chunkKey = ChunkKey.fromBlock(worldName, x, z);
        Set<ClaimId> candidateIds = chunkIndex.get(chunkKey);
        if (candidateIds == null || candidateIds.isEmpty()) {
            return Optional.empty();
        }

        for (ClaimId claimId : candidateIds) {
            Claim claim = claimsById.get(claimId);
            if (claim != null && claim.getBox().worldName().equals(worldName) && claim.containsBlock(x, y, z)) {
                return Optional.of(claim);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Claim> findById(ClaimId claimId) {
        Objects.requireNonNull(claimId, "claimId");
        return Optional.ofNullable(claimsById.get(claimId));
    }

    @Override
    public List<Claim> findOverlapping(ClaimBox box, ClaimId excludeId) {
        Objects.requireNonNull(box, "box");
        Objects.requireNonNull(excludeId, "excludeId");

        Set<ClaimId> candidates = new HashSet<>();
        for (ChunkKey chunkKey : box.intersectingChunks()) {
            Set<ClaimId> indexed = chunkIndex.get(chunkKey);
            if (indexed != null) {
                candidates.addAll(indexed);
            }
        }

        List<Claim> overlapping = new ArrayList<>();
        for (ClaimId claimId : candidates) {
            if (excludeId != null && claimId.equals(excludeId)) {
                continue;
            }
            Claim claim = claimsById.get(claimId);
            if (claim != null && claim.getBox().overlaps(box)) {
                overlapping.add(claim);
            }
        }
        return List.copyOf(overlapping);
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
    public int countBlocksByOwner(UUID owner) {
        Objects.requireNonNull(owner, "owner");
        int total = 0;
        for (Claim claim : claimsById.values()) {
            if (claim.getOwner().equals(owner)) {
                total += claim.blockCount();
            }
        }
        return total;
    }

    @Override
    public int countTotalBlocks() {
        int total = 0;
        for (Claim claim : claimsById.values()) {
            total += claim.blockCount();
        }
        return total;
    }

    private void addToIndex(Claim claim) {
        for (ChunkKey chunkKey : claim.getBox().intersectingChunks()) {
            chunkIndex.computeIfAbsent(chunkKey, ignored -> ConcurrentHashMap.newKeySet()).add(claim.getId());
        }
    }

    private void removeFromIndex(Claim claim) {
        for (ChunkKey chunkKey : claim.getBox().intersectingChunks()) {
            Set<ClaimId> indexed = chunkIndex.get(chunkKey);
            if (indexed != null) {
                indexed.remove(claim.getId());
                if (indexed.isEmpty()) {
                    chunkIndex.remove(chunkKey);
                }
            }
        }
    }
}
