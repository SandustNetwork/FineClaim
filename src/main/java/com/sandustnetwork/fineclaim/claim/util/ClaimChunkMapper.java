package com.sandustnetwork.fineclaim.claim.util;

import com.sandustnetwork.fineclaim.claim.domain.ClaimChunk;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.Objects;

public final class ClaimChunkMapper {

    private ClaimChunkMapper() {
    }

    public static ClaimChunk fromLocation(Location location) {
        Objects.requireNonNull(location, "location");
        return fromChunk(location.getChunk());
    }

    public static ClaimChunk fromChunk(Chunk chunk) {
        Objects.requireNonNull(chunk, "chunk");
        return new ClaimChunk(
                chunk.getWorld().getName(),
                chunk.getX(),
                chunk.getZ()
        );
    }
}
