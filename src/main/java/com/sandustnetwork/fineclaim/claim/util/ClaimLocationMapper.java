package com.sandustnetwork.fineclaim.claim.util;

import com.sandustnetwork.fineclaim.claim.domain.BlockPos;
import org.bukkit.Location;

import java.util.Objects;

public final class ClaimLocationMapper {

    private ClaimLocationMapper() {
    }

    public static BlockPos fromLocation(Location location) {
        Objects.requireNonNull(location, "location");
        return new BlockPos(
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }

    public static BlockPos fromBlockLocation(Location location) {
        Objects.requireNonNull(location, "location");
        return new BlockPos(
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }
}
