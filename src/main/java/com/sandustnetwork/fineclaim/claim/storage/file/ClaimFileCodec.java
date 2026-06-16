package com.sandustnetwork.fineclaim.claim.storage.file;

import com.sandustnetwork.fineclaim.claim.domain.Claim;
import com.sandustnetwork.fineclaim.claim.domain.ClaimBox;
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
    private static final String BOX_KEY = "box";
    private static final String CHUNKS_KEY = "chunks";

    private final int migrationMinY;
    private final int migrationMaxY;

    public ClaimFileCodec() {
        this(-64, 320);
    }

    public ClaimFileCodec(int migrationMinY, int migrationMaxY) {
        this.migrationMinY = migrationMinY;
        this.migrationMaxY = migrationMaxY;
    }

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
        ClaimBox box = decodeBox(entry);

        return new Claim(new ClaimId(id), box, owner, createdAt, trustedPlayers);
    }

    private ClaimBox decodeBox(Map<?, ?> entry) {
        Object rawBox = entry.get(BOX_KEY);
        if (rawBox instanceof Map<?, ?> boxEntry) {
            return decodeBoxEntry(boxEntry);
        }

        return migrateChunksToBox(entry);
    }

    private ClaimBox decodeBoxEntry(Map<?, ?> boxEntry) {
        String worldName = requireString(boxEntry, "worldName");
        int minX = requireInt(boxEntry, "minX");
        int minY = requireInt(boxEntry, "minY");
        int minZ = requireInt(boxEntry, "minZ");
        int maxX = requireInt(boxEntry, "maxX");
        int maxY = requireInt(boxEntry, "maxY");
        int maxZ = requireInt(boxEntry, "maxZ");
        return new ClaimBox(worldName, minX, minY, minZ, maxX, maxY, maxZ);
    }

    private ClaimBox migrateChunksToBox(Map<?, ?> entry) {
        Object rawChunks = entry.get(CHUNKS_KEY);
        if (rawChunks instanceof List<?> chunkList) {
            return mergeChunkEntries(chunkList);
        }
        return chunkToBox(decodeLegacyChunk(entry));
    }

    private ClaimBox mergeChunkEntries(List<?> chunkList) {
        if (chunkList.isEmpty()) {
            throw new IllegalArgumentException("Claim must contain at least one chunk");
        }

        String worldName = null;
        int minChunkX = Integer.MAX_VALUE;
        int maxChunkX = Integer.MIN_VALUE;
        int minChunkZ = Integer.MAX_VALUE;
        int maxChunkZ = Integer.MIN_VALUE;

        for (Object rawChunk : chunkList) {
            if (!(rawChunk instanceof Map<?, ?> chunkEntry)) {
                throw new IllegalArgumentException("Each chunk entry must be a map");
            }
            String chunkWorld = requireString(chunkEntry, "worldName");
            int chunkX = requireInt(chunkEntry, "chunkX");
            int chunkZ = requireInt(chunkEntry, "chunkZ");
            if (worldName == null) {
                worldName = chunkWorld;
            } else if (!worldName.equals(chunkWorld)) {
                throw new IllegalArgumentException("All chunks in a claim must share the same world");
            }
            minChunkX = Math.min(minChunkX, chunkX);
            maxChunkX = Math.max(maxChunkX, chunkX);
            minChunkZ = Math.min(minChunkZ, chunkZ);
            maxChunkZ = Math.max(maxChunkZ, chunkZ);
        }

        return new ClaimBox(
                worldName,
                minChunkX << 4,
                migrationMinY,
                minChunkZ << 4,
                (maxChunkX << 4) + 15,
                migrationMaxY,
                (maxChunkZ << 4) + 15
        );
    }

    private ClaimBox chunkToBox(LegacyChunk chunk) {
        return new ClaimBox(
                chunk.worldName(),
                chunk.chunkX() << 4,
                migrationMinY,
                chunk.chunkZ() << 4,
                (chunk.chunkX() << 4) + 15,
                migrationMaxY,
                (chunk.chunkZ() << 4) + 15
        );
    }

    private LegacyChunk decodeLegacyChunk(Map<?, ?> entry) {
        String worldName = requireString(entry, "worldName");
        int chunkX = requireInt(entry, "chunkX");
        int chunkZ = requireInt(entry, "chunkZ");
        return new LegacyChunk(worldName, chunkX, chunkZ);
    }

    private Map<String, Object> encodeClaim(Claim claim) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("id", claim.getId().value().toString());
        entry.put("owner", claim.getOwner().toString());
        entry.put("createdAt", claim.getCreatedAt().toString());
        entry.put("trustedPlayers", encodeTrustedPlayers(claim.getTrustedPlayers()));
        entry.put(BOX_KEY, encodeBox(claim.getBox()));
        return entry;
    }

    private Map<String, Object> encodeBox(ClaimBox box) {
        Map<String, Object> boxEntry = new HashMap<>();
        boxEntry.put("worldName", box.worldName());
        boxEntry.put("minX", box.minX());
        boxEntry.put("minY", box.minY());
        boxEntry.put("minZ", box.minZ());
        boxEntry.put("maxX", box.maxX());
        boxEntry.put("maxY", box.maxY());
        boxEntry.put("maxZ", box.maxZ());
        return boxEntry;
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

    private record LegacyChunk(String worldName, int chunkX, int chunkZ) {
    }
}
