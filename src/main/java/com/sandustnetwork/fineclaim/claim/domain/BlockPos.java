package com.sandustnetwork.fineclaim.claim.domain;

import java.util.Objects;

public record BlockPos(String worldName, int x, int y, int z) {

    public BlockPos {
        Objects.requireNonNull(worldName, "worldName");
        if (worldName.isBlank()) {
            throw new IllegalArgumentException("worldName must not be blank");
        }
    }
}
