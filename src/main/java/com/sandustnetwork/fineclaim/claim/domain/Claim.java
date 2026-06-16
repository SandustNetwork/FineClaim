package com.sandustnetwork.fineclaim.claim.domain;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Claim {

    private final ClaimId id;
    private final Set<ClaimChunk> chunks;
    private final UUID owner;
    private final Instant createdAt;
    private final Set<UUID> trustedPlayers;

    public Claim(
            ClaimId id,
            Set<ClaimChunk> chunks,
            UUID owner,
            Instant createdAt,
            Set<UUID> trustedPlayers
    ) {
        this.id = Objects.requireNonNull(id, "id");
        Objects.requireNonNull(chunks, "chunks");
        if (chunks.isEmpty()) {
            throw new IllegalArgumentException("Claim must contain at least one chunk");
        }
        this.chunks = Set.copyOf(chunks);
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

    public Set<ClaimChunk> getChunks() {
        return chunks;
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

    public int chunkCount() {
        return chunks.size();
    }

    public boolean containsChunk(ClaimChunk chunk) {
        Objects.requireNonNull(chunk, "chunk");
        return chunks.contains(chunk);
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

    public Claim withChunkAdded(ClaimChunk chunk) {
        Objects.requireNonNull(chunk, "chunk");
        if (chunks.contains(chunk)) {
            return this;
        }
        Set<ClaimChunk> updatedChunks = new HashSet<>(chunks);
        updatedChunks.add(chunk);
        return new Claim(id, updatedChunks, owner, createdAt, trustedPlayers);
    }

    public Claim withChunkRemoved(ClaimChunk chunk) {
        Objects.requireNonNull(chunk, "chunk");
        if (!chunks.contains(chunk)) {
            return this;
        }
        Set<ClaimChunk> updatedChunks = new HashSet<>(chunks);
        updatedChunks.remove(chunk);
        if (updatedChunks.isEmpty()) {
            throw new IllegalArgumentException("Claim must contain at least one chunk");
        }
        return new Claim(id, updatedChunks, owner, createdAt, trustedPlayers);
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
        return new Claim(id, chunks, owner, createdAt, updatedTrustedPlayers);
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
        return new Claim(id, chunks, owner, createdAt, updatedTrustedPlayers);
    }
}
