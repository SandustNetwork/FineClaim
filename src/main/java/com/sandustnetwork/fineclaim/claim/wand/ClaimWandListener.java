package com.sandustnetwork.fineclaim.claim.wand;

import com.sandustnetwork.fineclaim.claim.util.ClaimLocationMapper;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Objects;

public final class ClaimWandListener implements Listener {

    private final ClaimWandManager wandManager;

    public ClaimWandListener(ClaimWandManager wandManager) {
        this.wandManager = Objects.requireNonNull(wandManager, "wandManager");
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isActiveWandUse(player)) {
            return;
        }

        event.setCancelled(true);
        wandManager.setPointA(player, ClaimLocationMapper.fromBlockLocation(event.getBlock().getLocation()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        if (!isActiveWandUse(player)) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);

        wandManager.setPointB(player, ClaimLocationMapper.fromBlockLocation(clickedBlock.getLocation()));
    }

    private boolean isActiveWandUse(Player player) {
        return wandManager.isClaimWand(player.getInventory().getItemInMainHand())
                && wandManager.getSession(player).isPresent();
    }
}
