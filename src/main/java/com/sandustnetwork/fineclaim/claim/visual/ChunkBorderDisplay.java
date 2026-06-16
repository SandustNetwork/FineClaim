package com.sandustnetwork.fineclaim.claim.visual;

import com.sandustnetwork.fineclaim.claim.domain.ClaimChunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public final class ChunkBorderDisplay {

    private final JavaPlugin plugin;

    public ChunkBorderDisplay(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public void showChunks(Player player, Set<ClaimChunk> chunks, Material material, Consumer<List<BlockDisplay>> callback) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(chunks, "chunks");
        Objects.requireNonNull(material, "material");
        Objects.requireNonNull(callback, "callback");

        if (chunks.isEmpty()) {
            callback.accept(List.of());
            return;
        }

        List<BlockDisplay> displays = new ArrayList<>();
        ClaimChunk firstChunk = chunks.iterator().next();
        World world = player.getWorld();
        if (!firstChunk.worldName().equals(world.getName())) {
            callback.accept(List.of());
            return;
        }

        scheduleChunkDisplays(player, world, chunks, material, displays, callback, 0, List.copyOf(chunks));
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

    private void scheduleChunkDisplays(
            Player player,
            World world,
            Set<ClaimChunk> allChunks,
            Material material,
            List<BlockDisplay> collectedDisplays,
            Consumer<List<BlockDisplay>> callback,
            int index,
            List<ClaimChunk> chunkList
    ) {
        if (index >= chunkList.size()) {
            callback.accept(List.copyOf(collectedDisplays));
            return;
        }

        ClaimChunk chunk = chunkList.get(index);
        plugin.getServer().getRegionScheduler().run(plugin, world, chunk.chunkX(), chunk.chunkZ(), task -> {
            collectedDisplays.addAll(spawnCornerMarkers(player, world, chunk, material));
            scheduleChunkDisplays(player, world, allChunks, material, collectedDisplays, callback, index + 1, chunkList);
        });
    }

    private List<BlockDisplay> spawnCornerMarkers(Player player, World world, ClaimChunk chunk, Material material) {
        int minX = chunk.chunkX() << 4;
        int minZ = chunk.chunkZ() << 4;
        int maxX = minX + 15;
        int maxZ = minZ + 15;

        List<BlockDisplay> displays = new ArrayList<>(12);
        displays.addAll(spawnCorner(player, world, material, minX, minZ, 1, 1));
        displays.addAll(spawnCorner(player, world, material, maxX, minZ, -1, 1));
        displays.addAll(spawnCorner(player, world, material, minX, maxZ, 1, -1));
        displays.addAll(spawnCorner(player, world, material, maxX, maxZ, -1, -1));
        return displays;
    }

    private List<BlockDisplay> spawnCorner(
            Player player,
            World world,
            Material material,
            int baseX,
            int baseZ,
            int offsetX,
            int offsetZ
    ) {
        int y = world.getHighestBlockYAt(baseX, baseZ) + 1;
        List<BlockDisplay> cornerDisplays = new ArrayList<>(3);

        cornerDisplays.add(spawnSingleDisplay(player, world, baseX, y, baseZ, material));
        cornerDisplays.add(spawnSingleDisplay(player, world, baseX + offsetX, y, baseZ, material));
        cornerDisplays.add(spawnSingleDisplay(player, world, baseX, y, baseZ + offsetZ, material));
        return cornerDisplays;
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
}
