package com.sandustnetwork.fineclaim.claim.visual;

import com.sandustnetwork.fineclaim.claim.config.ClaimSettings;
import com.sandustnetwork.fineclaim.claim.domain.ClaimBox;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class BoxBorderDisplay {

    private final JavaPlugin plugin;

    public BoxBorderDisplay(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public void drawBox(Player player, ClaimBox box, ClaimSettings settings, PreviewSession session) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(box, "box");
        Objects.requireNonNull(settings, "settings");
        Objects.requireNonNull(session, "session");

        World world = player.getWorld();
        if (!box.worldName().equals(world.getName())) {
            return;
        }

        player.getScheduler().run(plugin, task -> {
            if (!session.isActive()) {
                return;
            }
            drawBoxOnRegionThread(player, world, box, settings);
        }, null);
    }

    private void drawBoxOnRegionThread(Player player, World world, ClaimBox box, ClaimSettings settings) {
        double minX = box.minX();
        double minY = box.minY();
        double minZ = box.minZ();
        double maxX = box.maxX() + 1.0;
        double maxY = box.maxY() + 1.0;
        double maxZ = box.maxZ() + 1.0;

        double[][] corners = {
                {minX, minY, minZ},
                {maxX, minY, minZ},
                {minX, minY, maxZ},
                {maxX, minY, maxZ},
                {minX, maxY, minZ},
                {maxX, maxY, minZ},
                {minX, maxY, maxZ},
                {maxX, maxY, maxZ}
        };

        BorderColorPalette palette = settings.borderColorPalette();
        Particle accentParticle = settings.cornerParticle();

        for (int cornerIndex = 0; cornerIndex < corners.length; cornerIndex++) {
            double[] corner = corners[cornerIndex];
            spawnCorner(player, world, corner[0], corner[1], corner[2], palette, cornerIndex, accentParticle);
        }

        int[][] edges = {
                {0, 1}, {1, 3}, {3, 2}, {2, 0},
                {4, 5}, {5, 7}, {7, 6}, {6, 4},
                {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };

        for (int edgeIndex = 0; edgeIndex < edges.length; edgeIndex++) {
            int[] edge = edges[edgeIndex];
            double[] start = corners[edge[0]];
            double[] end = corners[edge[1]];
            drawLine(
                    player,
                    world,
                    start[0], start[1], start[2],
                    end[0], end[1], end[2],
                    palette,
                    edgeIndex,
                    settings.particleSpacing(),
                    settings.maxParticlesPerEdge(),
                    accentParticle
            );
        }
    }

    private void spawnCorner(
            Player player,
            World world,
            double x,
            double y,
            double z,
            BorderColorPalette palette,
            int cornerIndex,
            Particle accentParticle
    ) {
        Location location = new Location(world, x, y, z);
        for (int offset = 0; offset < palette.colors().size(); offset++) {
            Color color = palette.colorForCorner(cornerIndex + offset);
            player.spawnParticle(
                    Particle.DUST,
                    location,
                    2,
                    0.08,
                    0.08,
                    0.08,
                    0.0,
                    new Particle.DustOptions(color, 1.4f)
            );
        }
        player.spawnParticle(accentParticle, location, 2, 0.04, 0.04, 0.04, 0.0);
    }

    private void drawLine(
            Player player,
            World world,
            double x1,
            double y1,
            double z1,
            double x2,
            double y2,
            double z2,
            BorderColorPalette palette,
            int edgeIndex,
            double spacing,
            int maxSteps,
            Particle accentParticle
    ) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length == 0.0) {
            return;
        }

        int steps = Math.max(1, (int) Math.ceil(length / spacing));
        steps = Math.min(steps, maxSteps);

        for (int step = 0; step <= steps; step++) {
            double progress = step / (double) steps;
            Location location = new Location(
                    world,
                    x1 + dx * progress,
                    y1 + dy * progress,
                    z1 + dz * progress
            );
            Color color = palette.colorForEdge(edgeIndex, progress);
            player.spawnParticle(
                    Particle.DUST,
                    location,
                    1,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    new Particle.DustOptions(color, 1.1f)
            );
            if (step % 4 == 0) {
                player.spawnParticle(accentParticle, location, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }
}
