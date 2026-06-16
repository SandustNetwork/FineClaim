package com.sandustnetwork.fineclaim.claim.storage.file;

import com.sandustnetwork.fineclaim.claim.domain.Claim;
import com.sandustnetwork.fineclaim.claim.domain.ClaimChunk;
import com.sandustnetwork.fineclaim.claim.domain.ClaimId;
import com.sandustnetwork.fineclaim.claim.storage.ClaimRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class FileClaimRepository implements ClaimRepository {

    private static final String CLAIMS_FILE_NAME = "claims.yml";

    private final Path claimsFile;
    private final ClaimFileCodec codec;
    private final Logger logger;
    private final Map<ClaimId, Claim> claimsById;
    private final Map<ClaimChunk, ClaimId> claimIndex;

    public FileClaimRepository(Path dataFolder, Logger logger) {
        this(dataFolder, new ClaimFileCodec(), logger);
    }

    FileClaimRepository(Path dataFolder, ClaimFileCodec codec, Logger logger) {
        Objects.requireNonNull(dataFolder, "dataFolder");
        this.codec = Objects.requireNonNull(codec, "codec");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.claimsFile = dataFolder.resolve(CLAIMS_FILE_NAME);
        this.claimsById = new ConcurrentHashMap<>();
        this.claimIndex = new ConcurrentHashMap<>();

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
    public Optional<Claim> findByChunk(ClaimChunk chunk) {
        Objects.requireNonNull(chunk, "chunk");
        ClaimId claimId = claimIndex.get(chunk);
        if (claimId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(claimsById.get(claimId));
    }

    @Override
    public Optional<Claim> findById(ClaimId claimId) {
        Objects.requireNonNull(claimId, "claimId");
        return Optional.ofNullable(claimsById.get(claimId));
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
    public int countChunksByOwner(UUID owner) {
        Objects.requireNonNull(owner, "owner");
        int total = 0;
        for (Claim claim : claimsById.values()) {
            if (claim.getOwner().equals(owner)) {
                total += claim.chunkCount();
            }
        }
        return total;
    }

    @Override
    public int countTotalChunks() {
        int total = 0;
        for (Claim claim : claimsById.values()) {
            total += claim.chunkCount();
        }
        return total;
    }

    public int reloadFromFile() {
        loadClaimsIntoMemory();
        return countTotalChunks();
    }

    private void loadClaimsIntoMemory() {
        claimsById.clear();
        claimIndex.clear();

        try {
            List<Claim> loadedClaims = codec.read(claimsFile);
            for (Claim claim : loadedClaims) {
                claimsById.put(claim.getId(), claim);
                addToIndex(claim);
            }
            logger.info("Loaded " + loadedClaims.size() + " claim region(s) ("
                    + countTotalChunks() + " chunk(s)) from " + claimsFile.getFileName() + ".");
        } catch (IOException exception) {
            logger.warning("Failed to read claims file '" + claimsFile + "': " + exception.getMessage());
        } catch (RuntimeException exception) {
            logger.warning("Failed to parse claims file '" + claimsFile + "': " + exception.getMessage());
        }
    }

    private void addToIndex(Claim claim) {
        for (ClaimChunk chunk : claim.getChunks()) {
            claimIndex.put(chunk, claim.getId());
        }
    }

    private void removeFromIndex(Claim claim) {
        for (ClaimChunk chunk : claim.getChunks()) {
            claimIndex.remove(chunk);
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
