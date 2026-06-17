package com.sandustnetwork.fineclaim.claim.command;

import com.sandustnetwork.fineclaim.claim.application.ClaimOperationResult;
import com.sandustnetwork.fineclaim.claim.application.ClaimService;
import com.sandustnetwork.fineclaim.claim.util.ClaimLocationMapper;
import com.sandustnetwork.fineclaim.claim.util.FineClaimMessages;
import com.sandustnetwork.fineclaim.permission.FineClaimPermission;
import com.sandustnetwork.fineclaim.permission.PermissionChecker;
import org.bukkit.entity.Player;

import java.util.Objects;

final class ClaimUnclaimHandler {

    private final ClaimService claimService;
    private final PermissionChecker permissionChecker;

    ClaimUnclaimHandler(ClaimService claimService, PermissionChecker permissionChecker) {
        this.claimService = Objects.requireNonNull(claimService, "claimService");
        this.permissionChecker = Objects.requireNonNull(permissionChecker, "permissionChecker");
    }

    void handle(Player player) {
        if (!permissionChecker.hasPermission(player, FineClaimPermission.COMMAND_UNCLAIM)) {
            FineClaimMessages.sendError(player, "You do not have permission to use this command.");
            return;
        }

        var location = ClaimLocationMapper.fromLocation(player.getLocation());
        ClaimOperationResult result = claimService.deleteAtBlock(
                location.worldName(),
                location.x(),
                location.y(),
                location.z(),
                player.getUniqueId()
        );
        ClaimCommand.sendOperationResult(player, result);
    }
}
