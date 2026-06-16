package com.sandustnetwork.fineclaim.claim.domain;

import java.util.Objects;

public record ChunkKey(String worldName, int chunkX, int chunkZ) {

    public ChunkKey {
        Objects.requireNonNull(worldName, "worldName");
        if (worldName.isBlank()) {
            throw new IllegalArgumentException("worldName must not be blank");
        }
    }

    public static ChunkKey fromBlock(String worldName, int blockX, int blockZ) {
        return new ChunkKey(worldName, blockX >> 4, blockZ >> 4);
    }
}
