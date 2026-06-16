package com.sandustnetwork.fineclaim.claim.config;

import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.sandustnetwork.fineclaim.claim.visual.BorderColorPalette;

import java.util.Objects;

public final class ClaimSettings {

    private final int maxBlocksPerMember;
    private final int maxBlocksPerServer;
    private final int previewDisplaySeconds;
    private final Particle cornerParticle;
    private final BorderColorPalette borderColorPalette;
    private final double particleSpacing;
    private final int maxParticlesPerEdge;
    private final long borderRefreshTicks;
    private final int minClaimHeight;
    private final int maxClaimHeight;

    public ClaimSettings(
            int maxBlocksPerMember,
            int maxBlocksPerServer,
            int previewDisplaySeconds,
            Particle cornerParticle,
            BorderColorPalette borderColorPalette,
            double particleSpacing,
            int maxParticlesPerEdge,
            long borderRefreshTicks,
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
        if (particleSpacing <= 0) {
            throw new IllegalArgumentException("ParticleSpacing must be positive");
        }
        if (maxParticlesPerEdge <= 0) {
            throw new IllegalArgumentException("MaxParticlesPerEdge must be positive");
        }
        if (borderRefreshTicks <= 0) {
            throw new IllegalArgumentException("BorderRefreshTicks must be positive");
        }
        if (minClaimHeight > maxClaimHeight) {
            throw new IllegalArgumentException("MinClaimHeight must not exceed MaxClaimHeight");
        }
        this.maxBlocksPerMember = maxBlocksPerMember;
        this.maxBlocksPerServer = maxBlocksPerServer;
        this.previewDisplaySeconds = previewDisplaySeconds;
        this.cornerParticle = Objects.requireNonNull(cornerParticle, "cornerParticle");
        this.borderColorPalette = Objects.requireNonNull(borderColorPalette, "borderColorPalette");
        this.particleSpacing = particleSpacing;
        this.maxParticlesPerEdge = maxParticlesPerEdge;
        this.borderRefreshTicks = borderRefreshTicks;
        this.minClaimHeight = minClaimHeight;
        this.maxClaimHeight = maxClaimHeight;
    }

    public static ClaimSettings fromPlugin(JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        FileConfiguration config = plugin.getConfig();

        int maxBlocksPerMember = resolveMaxBlocksPerMember(config);
        int maxBlocksPerServer = resolveMaxBlocksPerServer(config);
        int previewDisplaySeconds = config.getInt("PreviewDisplaySeconds", 120);
        String cornerParticleName = config.getString("CornerParticle", "END_ROD");
        if (cornerParticleName == null && config.contains("BorderParticle")) {
            cornerParticleName = config.getString("BorderParticle", "END_ROD");
        }
        Particle cornerParticle = parseParticle(cornerParticleName, Particle.END_ROD);
        BorderColorPalette borderColorPalette = BorderColorPalette.fromConfig(config);
        double particleSpacing = config.getDouble("ParticleSpacing", 0.75);
        int maxParticlesPerEdge = config.getInt("MaxParticlesPerEdge", 64);
        long borderRefreshTicks = config.getLong("BorderRefreshTicks", 10L);
        int minClaimHeight = config.getInt("MinClaimHeight", -64);
        int maxClaimHeight = config.getInt("MaxClaimHeight", 320);

        return new ClaimSettings(
                maxBlocksPerMember,
                maxBlocksPerServer,
                previewDisplaySeconds,
                cornerParticle,
                borderColorPalette,
                particleSpacing,
                maxParticlesPerEdge,
                borderRefreshTicks,
                minClaimHeight,
                maxClaimHeight
        );
    }

    private static Particle parseParticle(String name, Particle fallback) {
        if (name == null || name.isBlank()) {
            return fallback;
        }
        try {
            return Particle.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid particle name: " + name);
        }
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

    public Particle cornerParticle() {
        return cornerParticle;
    }

    public BorderColorPalette borderColorPalette() {
        return borderColorPalette;
    }

    public double particleSpacing() {
        return particleSpacing;
    }

    public int maxParticlesPerEdge() {
        return maxParticlesPerEdge;
    }

    public long borderRefreshTicks() {
        return borderRefreshTicks;
    }

    public int minClaimHeight() {
        return minClaimHeight;
    }

    public int maxClaimHeight() {
        return maxClaimHeight;
    }
}
