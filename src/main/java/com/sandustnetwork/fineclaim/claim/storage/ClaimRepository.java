package com.sandustnetwork.fineclaim.claim.storage;

import com.sandustnetwork.fineclaim.claim.domain.Claim;
import com.sandustnetwork.fineclaim.claim.domain.ClaimBox;
import com.sandustnetwork.fineclaim.claim.domain.ClaimId;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public interface ClaimRepository {

    Collection<Claim> findAllClaims();

    Optional<Claim> findByBlock(String worldName, int x, int y, int z);

    Optional<Claim> findById(ClaimId claimId);

    List<Claim> findOverlapping(ClaimBox box, ClaimId excludeId);

    void save(Claim claim);

    void delete(ClaimId claimId);

    int countBlocksByOwner(UUID owner);

    int countTotalBlocks();
}
