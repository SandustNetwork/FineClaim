package com.sandustnetwork.fineclaim.claim.domain;

import java.util.Objects;

public record ClaimChunk(String worldName, int chunkX, int chunkZ) {

    public ClaimChunk {
        Objects.requireNonNull(worldName, "worldName");
        if (worldName.isBlank()) {
            throw new IllegalArgumentException("worldName must not be blank");
        }
    }
}
