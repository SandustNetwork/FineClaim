package com.sandustnetwork.fineclaim.claim.storage;

import com.sandustnetwork.fineclaim.claim.domain.Claim;
import com.sandustnetwork.fineclaim.claim.domain.ClaimChunk;
import com.sandustnetwork.fineclaim.claim.domain.ClaimId;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ClaimRepository {

    Collection<Claim> findAllClaims();

    Optional<Claim> findByChunk(ClaimChunk chunk);

    Optional<Claim> findById(ClaimId claimId);

    void save(Claim claim);

    void delete(ClaimId claimId);

    int countChunksByOwner(UUID owner);

    int countTotalChunks();
}
