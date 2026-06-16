package com.sandustnetwork.fineclaim.claim.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class ClaimSettings {

    private final int maxBlocksPerMember;
    private final int maxBlocksPerServer;
    private final int previewDisplaySeconds;
    private final Material borderBlock;
    private final int minClaimHeight;
    private final int maxClaimHeight;

    public ClaimSettings(
            int maxBlocksPerMember,
            int maxBlocksPerServer,
            int previewDisplaySeconds,
            Material borderBlock,
            int minClaimHeight,
            int maxClaimHeight
    ) {
        if (maxBlocksPerMember <= 0) {
            throw new IllegalArgumentException("MaxBlocksPerMember must be positive");
        }
        if (maxBlocksPerServer <= 0) {
            throw new IllegalArgumentException("MaxBlocksPerServer must be positive");
        }
        if (previewDisplaySeconds <= 0) {
            throw new IllegalArgumentException("PreviewDisplaySeconds must be positive");
        }
        if (minClaimHeight > maxClaimHeight) {
            throw new IllegalArgumentException("MinClaimHeight must not exceed MaxClaimHeight");
        }
        this.maxBlocksPerMember = maxBlocksPerMember;
        this.maxBlocksPerServer = maxBlocksPerServer;
        this.previewDisplaySeconds = previewDisplaySeconds;
        this.borderBlock = Objects.requireNonNull(borderBlock, "borderBlock");
        if (!borderBlock.isBlock()) {
            throw new IllegalArgumentException("BorderBlock must be a block material");
        }
        this.minClaimHeight = minClaimHeight;
        this.maxClaimHeight = maxClaimHeight;
    }

    public static ClaimSettings fromPlugin(JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        FileConfiguration config = plugin.getConfig();

        int maxBlocksPerMember = resolveMaxBlocksPerMember(config);
        int maxBlocksPerServer = resolveMaxBlocksPerServer(config);
        int previewDisplaySeconds = config.getInt("PreviewDisplaySeconds", 120);
        String borderBlockName = config.getString("BorderBlock", "LIGHT_BLUE_STAINED_GLASS");
        int minClaimHeight = config.getInt("MinClaimHeight", -64);
        int maxClaimHeight = config.getInt("MaxClaimHeight", 320);

        Material borderBlock = Material.matchMaterial(borderBlockName);
        if (borderBlock == null) {
            throw new IllegalArgumentException("Invalid BorderBlock material: " + borderBlockName);
        }

        return new ClaimSettings(
                maxBlocksPerMember,
                maxBlocksPerServer,
                previewDisplaySeconds,
                borderBlock,
                minClaimHeight,
                maxClaimHeight
        );
    }

    private static int resolveMaxBlocksPerMember(FileConfiguration config) {
        if (config.contains("MaxBlocksPerMember")) {
            return config.getInt("MaxBlocksPerMember");
        }
        if (config.contains("MaxChunksPerMember")) {
            return config.getInt("MaxChunksPerMember") * 256;
        }
        return 4096;
    }

    private static int resolveMaxBlocksPerServer(FileConfiguration config) {
        if (config.contains("MaxBlocksPerServer")) {
            return config.getInt("MaxBlocksPerServer");
        }
        if (config.contains("MaxChunksPerServer")) {
            return config.getInt("MaxChunksPerServer") * 256;
        }
        return 2_500_000;
    }

    public int maxBlocksPerMember() {
        return maxBlocksPerMember;
    }

    public int maxBlocksPerServer() {
        return maxBlocksPerServer;
    }

    public int previewDisplaySeconds() {
        return previewDisplaySeconds;
    }

    public Material borderBlock() {
        return borderBlock;
    }

    public int minClaimHeight() {
        return minClaimHeight;
    }

    public int maxClaimHeight() {
        return maxClaimHeight;
    }
}
