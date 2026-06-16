package com.sandustnetwork.fineclaim.claim.storage;

import com.sandustnetwork.fineclaim.claim.domain.Claim;
import com.sandustnetwork.fineclaim.claim.domain.ClaimChunk;

import java.util.Map;
import java.util.Optional;

public interface ClaimRepository {

    Map<ClaimChunk, Claim> findAll();

    Optional<Claim> findByChunk(ClaimChunk chunk);

    void save(Claim claim);

    void deleteByChunk(ClaimChunk chunk);
}
