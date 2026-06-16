package com.sandustnetwork.fineclaim.claim.domain;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class ClaimChunkAdjacency {

    private ClaimChunkAdjacency() {
    }

    public static Optional<ClaimChunk> findAdjacentOwnedChunk(ClaimChunk target, Set<ClaimChunk> ownedChunks) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(ownedChunks, "ownedChunks");

        for (ClaimChunk neighbor : cardinalNeighbors(target)) {
            if (ownedChunks.contains(neighbor)) {
                return Optional.of(neighbor);
            }
        }
        return Optional.empty();
    }

    public static boolean isEdgeChunk(ClaimChunk chunk, Set<ClaimChunk> regionChunks) {
        Objects.requireNonNull(chunk, "chunk");
        Objects.requireNonNull(regionChunks, "regionChunks");
        if (!regionChunks.contains(chunk)) {
            return false;
        }
        for (ClaimChunk neighbor : cardinalNeighbors(chunk)) {
            if (!regionChunks.contains(neighbor)) {
                return true;
            }
        }
        return false;
    }

    public static Set<ClaimChunk> cardinalNeighbors(ClaimChunk chunk) {
        Objects.requireNonNull(chunk, "chunk");
        return Set.of(
                new ClaimChunk(chunk.worldName(), chunk.chunkX(), chunk.chunkZ() - 1),
                new ClaimChunk(chunk.worldName(), chunk.chunkX() + 1, chunk.chunkZ()),
                new ClaimChunk(chunk.worldName(), chunk.chunkX(), chunk.chunkZ() + 1),
                new ClaimChunk(chunk.worldName(), chunk.chunkX() - 1, chunk.chunkZ())
        );
    }
}
