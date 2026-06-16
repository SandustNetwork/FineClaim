package com.sandustnetwork.fineclaim.claim.storage.file;

import com.sandustnetwork.fineclaim.claim.domain.Claim;
import com.sandustnetwork.fineclaim.claim.domain.ClaimChunk;
import com.sandustnetwork.fineclaim.claim.domain.ClaimId;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ClaimFileCodec {

    private static final String CLAIMS_KEY = "claims";
    private static final String CHUNKS_KEY = "chunks";

    public List<Claim> read(Path file) throws IOException {
        Objects.requireNonNull(file, "file");
        if (!Files.exists(file)) {
            return List.of();
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file.toFile());
        List<Map<?, ?>> claimEntries = config.getMapList(CLAIMS_KEY);
        List<Claim> claims = new ArrayList<>();

        for (Map<?, ?> entry : claimEntries) {
            claims.add(decodeClaim(entry));
        }

        return List.copyOf(claims);
    }

    public void write(Path file, Collection<Claim> claims) throws IOException {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(claims, "claims");

        Path parentDirectory = file.getParent();
        if (parentDirectory != null) {
            Files.createDirectories(parentDirectory);
        }

        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> claimEntries = new ArrayList<>();
        for (Claim claim : claims) {
            claimEntries.add(encodeClaim(claim));
        }
        config.set(CLAIMS_KEY, claimEntries);
        config.save(file.toFile());
    }

    private Claim decodeClaim(Map<?, ?> entry) {
        UUID id = UUID.fromString(requireString(entry, "id"));
        UUID owner = UUID.fromString(requireString(entry, "owner"));
        Instant createdAt = Instant.parse(requireString(entry, "createdAt"));
        Set<UUID> trustedPlayers = decodeTrustedPlayers(entry.get("trustedPlayers"));
        Set<ClaimChunk> chunks = decodeChunks(entry);

        return new Claim(new ClaimId(id), chunks, owner, createdAt, trustedPlayers);
    }

    private Set<ClaimChunk> decodeChunks(Map<?, ?> entry) {
        Object rawChunks = entry.get(CHUNKS_KEY);
        if (rawChunks instanceof List<?> chunkList) {
            Set<ClaimChunk> chunks = new HashSet<>();
            for (Object rawChunk : chunkList) {
                if (!(rawChunk instanceof Map<?, ?> chunkEntry)) {
                    throw new IllegalArgumentException("Each chunk entry must be a map");
                }
                chunks.add(decodeChunkEntry(chunkEntry));
            }
            if (chunks.isEmpty()) {
                throw new IllegalArgumentException("Claim must contain at least one chunk");
            }
            return Set.copyOf(chunks);
        }

        return Set.of(decodeLegacyChunk(entry));
    }

    private ClaimChunk decodeLegacyChunk(Map<?, ?> entry) {
        String worldName = requireString(entry, "worldName");
        int chunkX = requireInt(entry, "chunkX");
        int chunkZ = requireInt(entry, "chunkZ");
        return new ClaimChunk(worldName, chunkX, chunkZ);
    }

    private ClaimChunk decodeChunkEntry(Map<?, ?> chunkEntry) {
        String worldName = requireString(chunkEntry, "worldName");
        int chunkX = requireInt(chunkEntry, "chunkX");
        int chunkZ = requireInt(chunkEntry, "chunkZ");
        return new ClaimChunk(worldName, chunkX, chunkZ);
    }

    private Map<String, Object> encodeClaim(Claim claim) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("id", claim.getId().value().toString());
        entry.put("owner", claim.getOwner().toString());
        entry.put("createdAt", claim.getCreatedAt().toString());
        entry.put("trustedPlayers", encodeTrustedPlayers(claim.getTrustedPlayers()));
        entry.put(CHUNKS_KEY, encodeChunks(claim.getChunks()));
        return entry;
    }

    private List<Map<String, Object>> encodeChunks(Set<ClaimChunk> chunks) {
        List<Map<String, Object>> encodedChunks = new ArrayList<>(chunks.size());
        for (ClaimChunk chunk : chunks) {
            Map<String, Object> chunkEntry = new HashMap<>();
            chunkEntry.put("worldName", chunk.worldName());
            chunkEntry.put("chunkX", chunk.chunkX());
            chunkEntry.put("chunkZ", chunk.chunkZ());
            encodedChunks.add(chunkEntry);
        }
        return encodedChunks;
    }

    private Set<UUID> decodeTrustedPlayers(Object rawTrustedPlayers) {
        if (rawTrustedPlayers == null) {
            return Set.of();
        }
        if (!(rawTrustedPlayers instanceof List<?> trustedList)) {
            throw new IllegalArgumentException("trustedPlayers must be a list");
        }

        Set<UUID> trustedPlayers = new HashSet<>();
        for (Object rawPlayerId : trustedList) {
            if (!(rawPlayerId instanceof String playerId)) {
                throw new IllegalArgumentException("trustedPlayers entries must be strings");
            }
            trustedPlayers.add(UUID.fromString(playerId));
        }
        return Set.copyOf(trustedPlayers);
    }

    private List<String> encodeTrustedPlayers(Set<UUID> trustedPlayers) {
        List<String> encoded = new ArrayList<>(trustedPlayers.size());
        for (UUID trustedPlayer : trustedPlayers) {
            encoded.add(trustedPlayer.toString());
        }
        return encoded;
    }

    private String requireString(Map<?, ?> entry, String key) {
        Object value = entry.get(key);
        if (!(value instanceof String stringValue) || stringValue.isBlank()) {
            throw new IllegalArgumentException("Missing or invalid value for '" + key + "'");
        }
        return stringValue;
    }

    private int requireInt(Map<?, ?> entry, String key) {
        Object value = entry.get(key);
        if (!(value instanceof Number numberValue)) {
            throw new IllegalArgumentException("Missing or invalid value for '" + key + "'");
        }
        return numberValue.intValue();
    }
}
