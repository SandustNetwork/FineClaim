package com.sandustnetwork.fineclaim.claim.wand;

import com.sandustnetwork.fineclaim.claim.domain.BlockPos;
import com.sandustnetwork.fineclaim.claim.domain.ClaimBox;
import com.sandustnetwork.fineclaim.claim.domain.ClaimId;
import com.sandustnetwork.fineclaim.claim.util.FineClaimMessages;
import com.sandustnetwork.fineclaim.claim.visual.ClaimPreviewManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClaimWandManager implements Listener {

    private static final Material WAND_MATERIAL = Material.BRICK;

    public enum Mode {
        CREATE,
        RESIZE
    }

    private final JavaPlugin plugin;
    private final ClaimPreviewManager previewManager;
    private final NamespacedKey wandKey;
    private final Map<UUID, WandSession> sessions = new ConcurrentHashMap<>();

    public ClaimWandManager(JavaPlugin plugin, ClaimPreviewManager previewManager) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.previewManager = Objects.requireNonNull(previewManager, "previewManager");
        this.wandKey = new NamespacedKey(plugin, "claim-wand");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void startCreate(Player player) {
        Objects.requireNonNull(player, "player");
        endSession(player);
        sessions.put(player.getUniqueId(), new WandSession(Mode.CREATE, null, null, null));
        giveWand(player);
        FineClaimMessages.sendInfo(player, "Claim tool equipped. Left-click a block for point A, right-click for point B.");
        FineClaimMessages.sendInfo(player, "Use /claim confirm or /claim cancel when finished.");
    }

    public void startResize(Player player, ClaimId claimId) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(claimId, "claimId");
        endSession(player);
        sessions.put(player.getUniqueId(), new WandSession(Mode.RESIZE, claimId, null, null));
        giveWand(player);
        FineClaimMessages.sendInfo(player, "Resize mode active. Left-click point A, right-click point B.");
        FineClaimMessages.sendInfo(player, "Use /claim resize confirm or /claim resize cancel when finished.");
    }

    public Optional<WandSession> getSession(Player player) {
        Objects.requireNonNull(player, "player");
        return Optional.ofNullable(sessions.get(player.getUniqueId()));
    }

    public Optional<ClaimBox> getSelectedBox(Player player) {
        return getSession(player).flatMap(WandSession::selectedBox);
    }

    public void setPointA(Player player, BlockPos point) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(point, "point");
        WandSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }

        WandSession updated = session.withPointA(point);
        sessions.put(player.getUniqueId(), updated);
        FineClaimMessages.sendSuccess(player, "Point A set to (" + point.x() + ", " + point.y() + ", " + point.z() + ").");
        updatePreview(player, updated);
    }

    public void setPointB(Player player, BlockPos point) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(point, "point");
        WandSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }

        WandSession updated = session.withPointB(point);
        sessions.put(player.getUniqueId(), updated);
        FineClaimMessages.sendSuccess(player, "Point B set to (" + point.x() + ", " + point.y() + ", " + point.z() + ").");
        updatePreview(player, updated);
    }

    public void endSession(Player player) {
        Objects.requireNonNull(player, "player");
        sessions.remove(player.getUniqueId());
        previewManager.cancelPreview(player);
        revokeWand(player);
    }

    public void cleanupAll() {
        for (UUID playerId : sessions.keySet()) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                endSession(player);
            }
        }
        sessions.clear();
        HandlerList.unregisterAll(this);
    }

    public boolean isClaimWand(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() != WAND_MATERIAL) {
            return false;
        }
        ItemMeta meta = itemStack.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(wandKey, PersistentDataType.BYTE);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        endSession(event.getPlayer());
    }

    private void updatePreview(Player player, WandSession session) {
        session.selectedBox().ifPresent(box -> previewManager.startPreview(player, box));
    }

    private void giveWand(Player player) {
        revokeWand(player);
        ItemStack wand = new ItemStack(WAND_MATERIAL);
        ItemMeta meta = wand.getItemMeta();
        meta.displayName(Component.text("Claim Tool", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(wandKey, PersistentDataType.BYTE, (byte) 1);
        wand.setItemMeta(meta);
        player.getInventory().addItem(wand);
    }

    private void revokeWand(Player player) {
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (isClaimWand(itemStack)) {
                player.getInventory().remove(itemStack);
            }
        }
    }

    public record WandSession(Mode mode, ClaimId targetClaimId, BlockPos pointA, BlockPos pointB) {

        public WandSession withPointA(BlockPos point) {
            return new WandSession(mode, targetClaimId, point, pointB);
        }

        public WandSession withPointB(BlockPos point) {
            return new WandSession(mode, targetClaimId, pointA, point);
        }

        public Optional<ClaimBox> selectedBox() {
            if (pointA == null || pointB == null) {
                return Optional.empty();
            }
            if (!pointA.worldName().equals(pointB.worldName())) {
                return Optional.empty();
            }
            return Optional.of(ClaimBox.fromCorners(pointA, pointB));
        }
    }
}
