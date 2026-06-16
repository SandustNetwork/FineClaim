package com.sandustnetwork.fineclaim.claim.visual;

import com.sandustnetwork.fineclaim.claim.domain.ClaimBox;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class BoxBorderDisplay {

    private final JavaPlugin plugin;

    public BoxBorderDisplay(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public void showBox(Player player, ClaimBox box, Material material, Consumer<List<BlockDisplay>> callback) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(box, "box");
        Objects.requireNonNull(material, "material");
        Objects.requireNonNull(callback, "callback");

        World world = player.getWorld();
        if (!box.worldName().equals(world.getName())) {
            callback.accept(List.of());
            return;
        }

        List<CornerSpec> corners = List.of(
                new CornerSpec(box.minX(), box.minY(), box.minZ(), 1, 1),
                new CornerSpec(box.maxX(), box.minY(), box.minZ(), -1, 1),
                new CornerSpec(box.minX(), box.minY(), box.maxZ(), 1, -1),
                new CornerSpec(box.maxX(), box.minY(), box.maxZ(), -1, -1),
                new CornerSpec(box.minX(), box.maxY(), box.minZ(), 1, 1),
                new CornerSpec(box.maxX(), box.maxY(), box.minZ(), -1, 1),
                new CornerSpec(box.minX(), box.maxY(), box.maxZ(), 1, -1),
                new CornerSpec(box.maxX(), box.maxY(), box.maxZ(), -1, -1)
        );

        List<BlockDisplay> displays = new ArrayList<>();
        scheduleCorner(player, world, material, displays, corners, callback, 0);
    }

    public void removeDisplays(Player player, List<BlockDisplay> displays) {
        Objects.requireNonNull(player, "player");
        if (displays == null || displays.isEmpty()) {
            return;
        }

        BlockDisplay firstDisplay = displays.getFirst();
        World world = firstDisplay.getWorld();
        int chunkX = firstDisplay.getLocation().getBlockX() >> 4;
        int chunkZ = firstDisplay.getLocation().getBlockZ() >> 4;

        plugin.getServer().getRegionScheduler().run(plugin, world, chunkX, chunkZ, task -> {
            for (BlockDisplay display : displays) {
                if (display.isValid()) {
                    player.hideEntity(plugin, display);
                    display.remove();
                }
            }
        });
    }

    private void scheduleCorner(
            Player player,
            World world,
            Material material,
            List<BlockDisplay> collectedDisplays,
            List<CornerSpec> corners,
            Consumer<List<BlockDisplay>> callback,
            int index
    ) {
        if (index >= corners.size()) {
            callback.accept(List.copyOf(collectedDisplays));
            return;
        }

        CornerSpec corner = corners.get(index);
        plugin.getServer().getRegionScheduler().run(plugin, world, corner.x >> 4, corner.z >> 4, task -> {
            collectedDisplays.addAll(spawnCorner(player, world, material, corner));
            scheduleCorner(player, world, material, collectedDisplays, corners, callback, index + 1);
        });
    }

    private List<BlockDisplay> spawnCorner(Player player, World world, Material material, CornerSpec corner) {
        List<BlockDisplay> displays = new ArrayList<>(3);
        displays.add(spawnSingleDisplay(player, world, corner.x, corner.y, corner.z, material));
        displays.add(spawnSingleDisplay(player, world, corner.x + corner.offsetX, corner.y, corner.z, material));
        displays.add(spawnSingleDisplay(player, world, corner.x, corner.y, corner.z + corner.offsetZ, material));
        return displays;
    }

    private BlockDisplay spawnSingleDisplay(Player player, World world, int x, int y, int z, Material material) {
        Location location = new Location(world, x + 0.5, y, z + 0.5);
        BlockDisplay display = world.spawn(location, BlockDisplay.class, spawnedDisplay -> {
            spawnedDisplay.setBlock(material.createBlockData());
            spawnedDisplay.setPersistent(false);
            spawnedDisplay.setVisibleByDefault(false);
            spawnedDisplay.addScoreboardTag("fineclaim-border");
        });
        player.showEntity(plugin, display);
        return display;
    }

    private record CornerSpec(int x, int y, int z, int offsetX, int offsetZ) {
    }
}
