package com.sandustnetwork.fineclaim.claim.storage;

import com.sandustnetwork.fineclaim.claim.domain.Claim;
import com.sandustnetwork.fineclaim.claim.domain.ClaimChunk;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryClaimRepository implements ClaimRepository {

    private final Map<ClaimChunk, Claim> claims = new ConcurrentHashMap<>();

    @Override
    public Map<ClaimChunk, Claim> findAll() {
        return Map.copyOf(claims);
    }

    @Override
    public Optional<Claim> findByChunk(ClaimChunk chunk) {
        Objects.requireNonNull(chunk, "chunk");
        return Optional.ofNullable(claims.get(chunk));
    }

    @Override
    public void save(Claim claim) {
        Objects.requireNonNull(claim, "claim");
        claims.put(claim.getChunk(), claim);
    }

    @Override
    public void deleteByChunk(ClaimChunk chunk) {
        Objects.requireNonNull(chunk, "chunk");
        claims.remove(chunk);
    }
}
