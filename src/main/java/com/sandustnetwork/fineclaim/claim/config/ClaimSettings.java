package com.sandustnetwork.fineclaim.claim.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class ClaimSettings {

    private final int maxChunksPerMember;
    private final int maxChunksPerServer;
    private final int previewDisplaySeconds;
    private final Material borderBlock;

    public ClaimSettings(
            int maxChunksPerMember,
            int maxChunksPerServer,
            int previewDisplaySeconds,
            Material borderBlock
    ) {
        if (maxChunksPerMember <= 0) {
            throw new IllegalArgumentException("MaxChunksPerMember must be positive");
        }
        if (maxChunksPerServer <= 0) {
            throw new IllegalArgumentException("MaxChunksPerServer must be positive");
        }
        if (previewDisplaySeconds <= 0) {
            throw new IllegalArgumentException("PreviewDisplaySeconds must be positive");
        }
        this.maxChunksPerMember = maxChunksPerMember;
        this.maxChunksPerServer = maxChunksPerServer;
        this.previewDisplaySeconds = previewDisplaySeconds;
        this.borderBlock = Objects.requireNonNull(borderBlock, "borderBlock");
        if (!borderBlock.isBlock()) {
            throw new IllegalArgumentException("BorderBlock must be a block material");
        }
    }

    public static ClaimSettings fromPlugin(JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        FileConfiguration config = plugin.getConfig();

        int maxChunksPerMember = config.getInt("MaxChunksPerMember", 16);
        int maxChunksPerServer = config.getInt("MaxChunksPerServer", 10000);
        int previewDisplaySeconds = config.getInt("PreviewDisplaySeconds", 120);
        String borderBlockName = config.getString("BorderBlock", "LIGHT_BLUE_STAINED_GLASS");

        Material borderBlock = Material.matchMaterial(borderBlockName);
        if (borderBlock == null) {
            throw new IllegalArgumentException("Invalid BorderBlock material: " + borderBlockName);
        }

        return new ClaimSettings(maxChunksPerMember, maxChunksPerServer, previewDisplaySeconds, borderBlock);
    }

    public int maxChunksPerMember() {
        return maxChunksPerMember;
    }

    public int maxChunksPerServer() {
        return maxChunksPerServer;
    }

    public int previewDisplaySeconds() {
        return previewDisplaySeconds;
    }

    public Material borderBlock() {
        return borderBlock;
    }
}
