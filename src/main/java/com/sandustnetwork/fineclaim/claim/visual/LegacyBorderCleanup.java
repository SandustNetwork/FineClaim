package com.sandustnetwork.fineclaim.claim.visual;

import com.sandustnetwork.fineclaim.claim.domain.ClaimBox;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

final class LegacyBorderCleanup {

    static final String BORDER_TAG = "fineclaim-border";

    private LegacyBorderCleanup() {
    }

    static void removeInBox(JavaPlugin plugin, Player player, ClaimBox box) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(player, "player");
        if (box == null) {
            return;
        }

        player.getScheduler().run(plugin, task -> {
            World world = plugin.getServer().getWorld(box.worldName());
            if (world == null) {
                return;
            }

            for (BlockDisplay display : world.getEntitiesByClass(BlockDisplay.class)) {
                if (!display.getScoreboardTags().contains(BORDER_TAG)) {
                    continue;
                }
                if (!box.contains(
                        display.getLocation().getBlockX(),
                        display.getLocation().getBlockY(),
                        display.getLocation().getBlockZ()
                )) {
                    continue;
                }
                player.hideEntity(plugin, display);
                display.remove();
            }
        }, null);
    }

    static void removeAll(JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        plugin.getServer().getGlobalRegionScheduler().run(plugin, scheduledTask -> {
            for (World world : plugin.getServer().getWorlds()) {
                for (BlockDisplay display : world.getEntitiesByClass(BlockDisplay.class)) {
                    if (display.getScoreboardTags().contains(BORDER_TAG)) {
                        display.remove();
                    }
                }
            }
        });
    }
}
