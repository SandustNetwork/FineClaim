package com.sandustnetwork.fineclaim.claim.wand;

import com.sandustnetwork.fineclaim.claim.util.ClaimLocationMapper;
import com.sandustnetwork.fineclaim.claim.util.FineClaimMessages;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Objects;

public final class ClaimWandListener implements Listener {

    private final ClaimWandManager wandManager;

    public ClaimWandListener(ClaimWandManager wandManager) {
        this.wandManager = Objects.requireNonNull(wandManager, "wandManager");
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        if (!wandManager.isClaimWand(player.getInventory().getItemInMainHand())) {
            return;
        }

        if (wandManager.getSession(player).isEmpty()) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        event.setCancelled(true);

        if (action == Action.LEFT_CLICK_BLOCK) {
            wandManager.setPointA(player, ClaimLocationMapper.fromBlockLocation(clickedBlock.getLocation()));
            return;
        }

        wandManager.setPointB(player, ClaimLocationMapper.fromBlockLocation(clickedBlock.getLocation()));
    }
}
