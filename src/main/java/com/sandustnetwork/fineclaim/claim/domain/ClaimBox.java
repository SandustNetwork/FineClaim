package com.sandustnetwork.fineclaim.claim.domain;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class ClaimBox {

    private final String worldName;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;

    public ClaimBox(String worldName, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        Objects.requireNonNull(worldName, "worldName");
        if (worldName.isBlank()) {
            throw new IllegalArgumentException("worldName must not be blank");
        }
        if (minX > maxX || minY > maxY || minZ > maxZ) {
            throw new IllegalArgumentException("Box min coordinates must not exceed max coordinates");
        }
        this.worldName = worldName;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public static ClaimBox fromCorners(String worldName, int ax, int ay, int az, int bx, int by, int bz) {
        Objects.requireNonNull(worldName, "worldName");
        return new ClaimBox(
                worldName,
                Math.min(ax, bx),
                Math.min(ay, by),
                Math.min(az, bz),
                Math.max(ax, bx),
                Math.max(ay, by),
                Math.max(az, bz)
        );
    }

    public static ClaimBox fromCorners(BlockPos pointA, BlockPos pointB) {
        Objects.requireNonNull(pointA, "pointA");
        Objects.requireNonNull(pointB, "pointB");
        if (!pointA.worldName().equals(pointB.worldName())) {
            throw new IllegalArgumentException("Both corners must be in the same world");
        }
        return fromCorners(
                pointA.worldName(),
                pointA.x(), pointA.y(), pointA.z(),
                pointB.x(), pointB.y(), pointB.z()
        );
    }

    public String worldName() {
        return worldName;
    }

    public int minX() {
        return minX;
    }

    public int minY() {
        return minY;
    }

    public int minZ() {
        return minZ;
    }

    public int maxX() {
        return maxX;
    }

    public int maxY() {
        return maxY;
    }

    public int maxZ() {
        return maxZ;
    }

    public int sizeX() {
        return maxX - minX + 1;
    }

    public int sizeY() {
        return maxY - minY + 1;
    }

    public int sizeZ() {
        return maxZ - minZ + 1;
    }

    public int volume() {
        return sizeX() * sizeY() * sizeZ();
    }

    public boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    public boolean contains(BlockPos pos) {
        Objects.requireNonNull(pos, "pos");
        return worldName.equals(pos.worldName()) && contains(pos.x(), pos.y(), pos.z());
    }

    public boolean overlaps(ClaimBox other) {
        Objects.requireNonNull(other, "other");
        if (!worldName.equals(other.worldName)) {
            return false;
        }
        return minX <= other.maxX && maxX >= other.minX
                && minY <= other.maxY && maxY >= other.minY
                && minZ <= other.maxZ && maxZ >= other.minZ;
    }

    public Set<ChunkKey> intersectingChunks() {
        Set<ChunkKey> chunks = new HashSet<>();
        int minChunkX = minX >> 4;
        int maxChunkX = maxX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkZ = maxZ >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                chunks.add(new ChunkKey(worldName, chunkX, chunkZ));
            }
        }
        return Set.copyOf(chunks);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ClaimBox other)) {
            return false;
        }
        return worldName.equals(other.worldName)
                && minX == other.minX && minY == other.minY && minZ == other.minZ
                && maxX == other.maxX && maxY == other.maxY && maxZ == other.maxZ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, minX, minY, minZ, maxX, maxY, maxZ);
    }
}
