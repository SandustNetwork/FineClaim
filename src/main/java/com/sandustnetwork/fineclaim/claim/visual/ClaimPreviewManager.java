package com.sandustnetwork.fineclaim.claim.visual;

import com.sandustnetwork.fineclaim.claim.config.ClaimSettings;
import com.sandustnetwork.fineclaim.claim.domain.Claim;
import com.sandustnetwork.fineclaim.claim.domain.ClaimBox;
import com.sandustnetwork.fineclaim.claim.util.FineClaimMessages;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

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
        LegacyBorderCleanup.removeAll(plugin);
    }

    public void updateSettings(ClaimSettings settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    public void startPreview(Player player, ClaimBox box) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(box, "box");

        stopPreview(player);
        PreviewSession session = startAnimation(player, box);
        previewSessions.put(player.getUniqueId(), session);
    }

    public Optional<ClaimBox> getPreviewBox(Player player) {
        Objects.requireNonNull(player, "player");
        PreviewSession session = previewSessions.get(player.getUniqueId());
        if (session == null || !session.isActive()) {
            return Optional.empty();
        }
        return Optional.ofNullable(session.box());
    }

    public void cancelPreview(Player player) {
        stopPreview(player);
    }

    public void showClaimBorder(Player player, Claim claim) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(claim, "claim");

        stopPreview(player);
        PreviewSession session = startAnimation(player, claim.getBox());
        previewSessions.put(player.getUniqueId(), session);
        FineClaimMessages.sendInfo(player, "Claim border shown.");
    }

    public void cleanupAll() {
        for (UUID playerId : List.copyOf(previewSessions.keySet())) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                stopPreview(player);
            } else {
                PreviewSession session = previewSessions.remove(playerId);
                if (session != null) {
                    session.deactivate();
                }
            }
        }
        LegacyBorderCleanup.removeAll(plugin);
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        stopPreview(event.getPlayer());
    }

    private PreviewSession startAnimation(Player player, ClaimBox box) {
        ClaimSettings activeSettings = settings;
        PreviewSession[] sessionHolder = new PreviewSession[1];

        ScheduledTask animationTask = player.getScheduler().runAtFixedRate(
                plugin,
                task -> {
                    PreviewSession session = sessionHolder[0];
                    if (session == null || !session.isActive()) {
                        return;
                    }
                    borderDisplay.drawBox(player, box, activeSettings, session);
                },
                () -> {},
                1L,
                activeSettings.borderRefreshTicks()
        );

        ScheduledTask expiryTask = player.getScheduler().runDelayed(
                plugin,
                scheduledTask -> stopPreview(player),
                null,
                activeSettings.previewDisplaySeconds() * 20L
        );

        PreviewSession session = new PreviewSession(box, animationTask, expiryTask);
        sessionHolder[0] = session;
        borderDisplay.drawBox(player, box, activeSettings, session);
        return session;
    }

    private void stopPreview(Player player) {
        Objects.requireNonNull(player, "player");
        PreviewSession session = previewSessions.remove(player.getUniqueId());
        if (session == null) {
            return;
        }

        session.deactivate();
        LegacyBorderCleanup.removeInBox(plugin, player, session.box());
    }
}
