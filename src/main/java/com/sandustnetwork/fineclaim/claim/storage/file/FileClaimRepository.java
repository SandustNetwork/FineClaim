package com.sandustnetwork.fineclaim.claim.storage.file;

import com.sandustnetwork.fineclaim.claim.domain.Claim;
import com.sandustnetwork.fineclaim.claim.domain.ClaimChunk;
import com.sandustnetwork.fineclaim.claim.storage.ClaimRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class FileClaimRepository implements ClaimRepository {

    private static final String CLAIMS_FILE_NAME = "claims.yml";

    private final Path claimsFile;
    private final ClaimFileCodec codec;
    private final Logger logger;
    private final Map<ClaimChunk, Claim> claims;

    public FileClaimRepository(Path dataFolder, Logger logger) {
        this(dataFolder, new ClaimFileCodec(), logger);
    }

    FileClaimRepository(Path dataFolder, ClaimFileCodec codec, Logger logger) {
        Objects.requireNonNull(dataFolder, "dataFolder");
        this.codec = Objects.requireNonNull(codec, "codec");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.claimsFile = dataFolder.resolve(CLAIMS_FILE_NAME);

        try {
            Files.createDirectories(dataFolder);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create plugin data folder: " + dataFolder, exception);
        }

        this.claims = new ConcurrentHashMap<>(loadClaims());
    }

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
        flush();
    }

    @Override
    public void deleteByChunk(ClaimChunk chunk) {
        Objects.requireNonNull(chunk, "chunk");
        claims.remove(chunk);
        flush();
    }

    private Map<ClaimChunk, Claim> loadClaims() {
        try {
            Map<ClaimChunk, Claim> loadedClaims = codec.read(claimsFile);
            logger.info("Loaded " + loadedClaims.size() + " claim(s) from " + claimsFile.getFileName() + ".");
            return new HashMap<>(loadedClaims);
        } catch (IOException exception) {
            logger.warning("Failed to read claims file '" + claimsFile + "': " + exception.getMessage());
            return Map.of();
        } catch (RuntimeException exception) {
            logger.warning("Failed to parse claims file '" + claimsFile + "': " + exception.getMessage());
            return Map.of();
        }
    }

    private void flush() {
        try {
            codec.write(claimsFile, claims);
        } catch (IOException exception) {
            logger.severe("Failed to save claims to '" + claimsFile + "': " + exception.getMessage());
        }
    }
}
