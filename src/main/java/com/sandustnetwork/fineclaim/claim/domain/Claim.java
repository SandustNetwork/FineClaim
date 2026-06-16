package com.sandustnetwork.fineclaim.claim.domain;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Claim {

    private final ClaimId id;
    private final ClaimChunk chunk;
    private final UUID owner;
    private final Instant createdAt;
    private final Set<UUID> trustedPlayers;

    public Claim(
            ClaimId id,
            ClaimChunk chunk,
            UUID owner,
            Instant createdAt,
            Set<UUID> trustedPlayers
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.chunk = Objects.requireNonNull(chunk, "chunk");
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

    public ClaimChunk getChunk() {
        return chunk;
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
        return new Claim(id, chunk, owner, createdAt, updatedTrustedPlayers);
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
        return new Claim(id, chunk, owner, createdAt, updatedTrustedPlayers);
    }
}
