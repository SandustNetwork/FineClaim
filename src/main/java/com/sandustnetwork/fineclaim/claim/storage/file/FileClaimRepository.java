package com.sandustnetwork.fineclaim.claim.storage.file;

import com.sandustnetwork.fineclaim.claim.domain.Claim;
import com.sandustnetwork.fineclaim.claim.domain.ClaimBox;
import com.sandustnetwork.fineclaim.claim.domain.ClaimId;
import com.sandustnetwork.fineclaim.claim.domain.ChunkKey;
import com.sandustnetwork.fineclaim.claim.storage.ClaimRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class FileClaimRepository implements ClaimRepository {

    private static final String CLAIMS_FILE_NAME = "claims.yml";

    private final Path claimsFile;
    private final ClaimFileCodec codec;
    private final Logger logger;
    private final Map<ClaimId, Claim> claimsById;
    private final Map<ChunkKey, Set<ClaimId>> chunkIndex;

    public FileClaimRepository(Path dataFolder, Logger logger) {
        this(dataFolder, new ClaimFileCodec(), logger);
    }

    public FileClaimRepository(Path dataFolder, ClaimFileCodec codec, Logger logger) {
        Objects.requireNonNull(dataFolder, "dataFolder");
        this.codec = Objects.requireNonNull(codec, "codec");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.claimsFile = dataFolder.resolve(CLAIMS_FILE_NAME);
        this.claimsById = new ConcurrentHashMap<>();
        this.chunkIndex = new ConcurrentHashMap<>();

        try {
            Files.createDirectories(dataFolder);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create plugin data folder: " + dataFolder, exception);
        }

        loadClaimsIntoMemory();
    }

    @Override
    public Collection<Claim> findAllClaims() {
        return List.copyOf(claimsById.values());
    }

    @Override
    public Optional<Claim> findByBlock(String worldName, int x, int y, int z) {
        Objects.requireNonNull(worldName, "worldName");
        ChunkKey chunkKey = ChunkKey.fromBlock(worldName, x, z);
        Set<ClaimId> candidateIds = chunkIndex.get(chunkKey);
        if (candidateIds == null || candidateIds.isEmpty()) {
            return Optional.empty();
        }

        for (ClaimId claimId : candidateIds) {
            Claim claim = claimsById.get(claimId);
            if (claim != null && claim.getBox().worldName().equals(worldName) && claim.containsBlock(x, y, z)) {
                return Optional.of(claim);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Claim> findById(ClaimId claimId) {
        Objects.requireNonNull(claimId, "claimId");
        return Optional.ofNullable(claimsById.get(claimId));
    }

    @Override
    public List<Claim> findOverlapping(ClaimBox box, ClaimId excludeId) {
        Objects.requireNonNull(box, "box");
        Objects.requireNonNull(excludeId, "excludeId");

        Set<ClaimId> candidates = new HashSet<>();
        for (ChunkKey chunkKey : box.intersectingChunks()) {
            Set<ClaimId> indexed = chunkIndex.get(chunkKey);
            if (indexed != null) {
                candidates.addAll(indexed);
            }
        }

        List<Claim> overlapping = new ArrayList<>();
        for (ClaimId claimId : candidates) {
            if (excludeId != null && claimId.equals(excludeId)) {
                continue;
            }
            Claim claim = claimsById.get(claimId);
            if (claim != null && claim.getBox().overlaps(box)) {
                overlapping.add(claim);
            }
        }
        return List.copyOf(overlapping);
    }

    @Override
    public void save(Claim claim) {
        Objects.requireNonNull(claim, "claim");

        ClaimId claimId = claim.getId();
        Claim previousClaim = claimsById.get(claimId);
        if (previousClaim != null) {
            removeFromIndex(previousClaim);
        }

        claimsById.put(claimId, claim);
        addToIndex(claim);
        flush();
    }

    @Override
    public void delete(ClaimId claimId) {
        Objects.requireNonNull(claimId, "claimId");
        Claim removedClaim = claimsById.remove(claimId);
        if (removedClaim != null) {
            removeFromIndex(removedClaim);
            flush();
        }
    }

    @Override
    public int countBlocksByOwner(UUID owner) {
        Objects.requireNonNull(owner, "owner");
        int total = 0;
        for (Claim claim : claimsById.values()) {
            if (claim.getOwner().equals(owner)) {
                total += claim.blockCount();
            }
        }
        return total;
    }

    @Override
    public int countTotalBlocks() {
        int total = 0;
        for (Claim claim : claimsById.values()) {
            total += claim.blockCount();
        }
        return total;
    }

    public int reloadFromFile() {
        loadClaimsIntoMemory();
        return countTotalBlocks();
    }

    private void loadClaimsIntoMemory() {
        claimsById.clear();
        chunkIndex.clear();

        try {
            List<Claim> loadedClaims = codec.read(claimsFile);
            for (Claim claim : loadedClaims) {
                claimsById.put(claim.getId(), claim);
                addToIndex(claim);
            }
            logger.info("Loaded " + loadedClaims.size() + " claim(s) ("
                    + countTotalBlocks() + " block(s)) from " + claimsFile.getFileName() + ".");
        } catch (IOException exception) {
            logger.warning("Failed to read claims file '" + claimsFile + "': " + exception.getMessage());
        } catch (RuntimeException exception) {
            logger.warning("Failed to parse claims file '" + claimsFile + "': " + exception.getMessage());
        }
    }

    private void addToIndex(Claim claim) {
        for (ChunkKey chunkKey : claim.getBox().intersectingChunks()) {
            chunkIndex.computeIfAbsent(chunkKey, ignored -> ConcurrentHashMap.newKeySet()).add(claim.getId());
        }
    }

    private void removeFromIndex(Claim claim) {
        for (ChunkKey chunkKey : claim.getBox().intersectingChunks()) {
            Set<ClaimId> indexed = chunkIndex.get(chunkKey);
            if (indexed != null) {
                indexed.remove(claim.getId());
                if (indexed.isEmpty()) {
                    chunkIndex.remove(chunkKey);
                }
            }
        }
    }

    private void flush() {
        try {
            codec.write(claimsFile, claimsById.values());
        } catch (IOException exception) {
            logger.severe("Failed to save claims to '" + claimsFile + "': " + exception.getMessage());
        }
    }
}
