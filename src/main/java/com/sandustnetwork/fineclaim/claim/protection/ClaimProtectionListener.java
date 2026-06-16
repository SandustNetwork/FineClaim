package com.sandustnetwork.fineclaim.claim.protection;

import com.sandustnetwork.fineclaim.claim.util.FineClaimMessages;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

public final class ClaimProtectionListener implements Listener {

    private final ProtectionCheck protectionCheck;

    public ClaimProtectionListener(ProtectionCheck protectionCheck) {
        this.protectionCheck = Objects.requireNonNull(protectionCheck, "protectionCheck");
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        var location = event.getBlock().getLocation();
        ProtectionResult result = protectionCheck.checkBuild(
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                event.getPlayer()
        );
        if (!result.isAllowed()) {
            event.setCancelled(true);
            FineClaimMessages.sendError(event.getPlayer(), result.message());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        var location = event.getBlock().getLocation();
        ProtectionResult result = protectionCheck.checkBuild(
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                event.getPlayer()
        );
        if (!result.isAllowed()) {
            event.setCancelled(true);
            FineClaimMessages.sendError(event.getPlayer(), result.message());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        var location = event.getClickedBlock().getLocation();
        ProtectionResult result = protectionCheck.checkInteract(
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                event.getPlayer()
        );
        if (!result.isAllowed()) {
            event.setCancelled(true);
            FineClaimMessages.sendError(event.getPlayer(), result.message());
        }
    }
}
