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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ClaimFileCodec {

    private static final String CLAIMS_KEY = "claims";

    public Map<ClaimChunk, Claim> read(Path file) throws IOException {
        Objects.requireNonNull(file, "file");
        if (!Files.exists(file)) {
            return Map.of();
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file.toFile());
        List<Map<?, ?>> claimEntries = config.getMapList(CLAIMS_KEY);
        Map<ClaimChunk, Claim> claims = new HashMap<>();

        for (Map<?, ?> entry : claimEntries) {
            Claim claim = decodeClaim(entry);
            claims.put(claim.getChunk(), claim);
        }

        return Map.copyOf(claims);
    }

    public void write(Path file, Map<ClaimChunk, Claim> claims) throws IOException {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(claims, "claims");

        Path parentDirectory = file.getParent();
        if (parentDirectory != null) {
            Files.createDirectories(parentDirectory);
        }

        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> claimEntries = new ArrayList<>();
        for (Claim claim : claims.values()) {
            claimEntries.add(encodeClaim(claim));
        }
        config.set(CLAIMS_KEY, claimEntries);
        config.save(file.toFile());
    }

    private Claim decodeClaim(Map<?, ?> entry) {
        UUID id = UUID.fromString(requireString(entry, "id"));
        String worldName = requireString(entry, "worldName");
        int chunkX = requireInt(entry, "chunkX");
        int chunkZ = requireInt(entry, "chunkZ");
        UUID owner = UUID.fromString(requireString(entry, "owner"));
        Instant createdAt = Instant.parse(requireString(entry, "createdAt"));
        Set<UUID> trustedPlayers = decodeTrustedPlayers(entry.get("trustedPlayers"));

        ClaimChunk chunk = new ClaimChunk(worldName, chunkX, chunkZ);
        return new Claim(new ClaimId(id), chunk, owner, createdAt, trustedPlayers);
    }

    private Map<String, Object> encodeClaim(Claim claim) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("id", claim.getId().value().toString());
        entry.put("worldName", claim.getChunk().worldName());
        entry.put("chunkX", claim.getChunk().chunkX());
        entry.put("chunkZ", claim.getChunk().chunkZ());
        entry.put("owner", claim.getOwner().toString());
        entry.put("createdAt", claim.getCreatedAt().toString());
        entry.put("trustedPlayers", encodeTrustedPlayers(claim.getTrustedPlayers()));
        return entry;
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
