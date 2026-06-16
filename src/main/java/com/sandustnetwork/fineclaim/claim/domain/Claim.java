package com.sandustnetwork.fineclaim.claim.domain;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Claim {

    private final ClaimId id;
    private final ClaimBox box;
    private final UUID owner;
    private final Instant createdAt;
    private final Set<UUID> trustedPlayers;

    public Claim(
            ClaimId id,
            ClaimBox box,
            UUID owner,
            Instant createdAt,
            Set<UUID> trustedPlayers
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.box = Objects.requireNonNull(box, "box");
        this.owner = Objects.requireNonNull(owner, "owner");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(trustedPlayers, "trustedPlayers");
        if (trustedPlayers.contains(owner)) {
            throw new IllegalArgumentException("Owner cannot be in trusted players");
        }
        this.trustedPlayers = Set.copyOf(trustedPlayers);
    }

    public ClaimId getId() {
        return id;
    }

    public ClaimBox getBox() {
        return box;
    }

    public UUID getOwner() {
        return owner;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Set<UUID> getTrustedPlayers() {
        return trustedPlayers;
    }

    public int blockCount() {
        return box.volume();
    }

    public boolean containsBlock(int x, int y, int z) {
        return box.contains(x, y, z);
    }

    public boolean isOwner(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        return owner.equals(playerId);
    }

    public boolean isTrusted(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        return trustedPlayers.contains(playerId);
    }

    public boolean canBuild(UUID playerId) {
        return isOwner(playerId) || isTrusted(playerId);
    }

    public Claim withBox(ClaimBox newBox) {
        Objects.requireNonNull(newBox, "newBox");
        return new Claim(id, newBox, owner, createdAt, trustedPlayers);
    }

    public Claim trust(UUID target) {
        Objects.requireNonNull(target, "target");
        if (owner.equals(target)) {
            throw new IllegalArgumentException("Cannot trust the owner");
        }
        if (trustedPlayers.contains(target)) {
            return this;
        }
        Set<UUID> updatedTrustedPlayers = new HashSet<>(trustedPlayers);
        updatedTrustedPlayers.add(target);
        return new Claim(id, box, owner, createdAt, updatedTrustedPlayers);
    }

    public Claim untrust(UUID target) {
        Objects.requireNonNull(target, "target");
        if (owner.equals(target)) {
            throw new IllegalArgumentException("Cannot untrust the owner");
        }
        if (!trustedPlayers.contains(target)) {
            return this;
        }
        Set<UUID> updatedTrustedPlayers = new HashSet<>(trustedPlayers);
        updatedTrustedPlayers.remove(target);
        return new Claim(id, box, owner, createdAt, updatedTrustedPlayers);
    }
}
