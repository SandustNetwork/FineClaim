package com.sandustnetwork.fineclaim.claim.visual;

import com.sandustnetwork.fineclaim.claim.config.ClaimSettings;
import com.sandustnetwork.fineclaim.claim.domain.Claim;
import com.sandustnetwork.fineclaim.claim.domain.ClaimBox;
import com.sandustnetwork.fineclaim.claim.util.FineClaimMessages;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClaimPreviewManager implements Listener {

    private final JavaPlugin plugin;
    private final BoxBorderDisplay borderDisplay;
    private ClaimSettings settings;
    private final Map<UUID, PreviewSession> previewSessions = new ConcurrentHashMap<>();

    public ClaimPreviewManager(JavaPlugin plugin, BoxBorderDisplay borderDisplay, ClaimSettings settings) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.borderDisplay = Objects.requireNonNull(borderDisplay, "borderDisplay");
        this.settings = Objects.requireNonNull(settings, "settings");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void updateSettings(ClaimSettings settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    public void startPreview(Player player, ClaimBox box) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(box, "box");

        cancelPreview(player);
        borderDisplay.showBox(player, box, settings.borderBlock(), displays -> {
            PreviewSession session = new PreviewSession(box, displays);
            previewSessions.put(player.getUniqueId(), session);
            scheduleExpiry(player);
        });
    }

    public Optional<ClaimBox> getPreviewBox(Player player) {
        Objects.requireNonNull(player, "player");
        PreviewSession session = previewSessions.get(player.getUniqueId());
        if (session == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(session.box());
    }

    public void cancelPreview(Player player) {
        Objects.requireNonNull(player, "player");
        PreviewSession session = previewSessions.remove(player.getUniqueId());
        if (session != null) {
            borderDisplay.removeDisplays(player, session.displays());
        }
    }

    public void showClaimBorder(Player player, Claim claim) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(claim, "claim");

        cancelPreview(player);
        borderDisplay.showBox(player, claim.getBox(), settings.borderBlock(), displays -> {
            PreviewSession session = new PreviewSession(null, displays);
            previewSessions.put(player.getUniqueId(), session);
            scheduleExpiry(player);
            FineClaimMessages.sendInfo(player, "Claim border shown.");
        });
    }

    public void cleanupAll() {
        for (UUID playerId : List.copyOf(previewSessions.keySet())) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                cancelPreview(player);
            } else {
                PreviewSession session = previewSessions.remove(playerId);
                if (session != null) {
                    for (BlockDisplay display : session.displays()) {
                        if (display.isValid()) {
                            display.remove();
                        }
                    }
                }
            }
        }
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cancelPreview(event.getPlayer());
    }

    private void scheduleExpiry(Player player) {
        long delayTicks = settings.previewDisplaySeconds() * 20L;
        player.getScheduler().runDelayed(plugin, scheduledTask -> cancelPreview(player), null, delayTicks);
    }

    private record PreviewSession(ClaimBox box, List<BlockDisplay> displays) {
        private PreviewSession {
            displays = List.copyOf(new ArrayList<>(displays));
        }
    }
}
