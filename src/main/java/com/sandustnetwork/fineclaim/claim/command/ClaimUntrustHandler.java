package com.sandustnetwork.fineclaim.claim.command;

import com.sandustnetwork.fineclaim.claim.application.ClaimOperationResult;
import com.sandustnetwork.fineclaim.claim.application.ClaimService;
import com.sandustnetwork.fineclaim.claim.util.ClaimLocationMapper;
import com.sandustnetwork.fineclaim.claim.util.FineClaimMessages;
import com.sandustnetwork.fineclaim.permission.FineClaimPermission;
import com.sandustnetwork.fineclaim.permission.PermissionChecker;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

final class ClaimUntrustHandler {

    private final ClaimService claimService;
    private final PermissionChecker permissionChecker;

    ClaimUntrustHandler(ClaimService claimService, PermissionChecker permissionChecker) {
        this.claimService = Objects.requireNonNull(claimService, "claimService");
        this.permissionChecker = Objects.requireNonNull(permissionChecker, "permissionChecker");
    }

    void handle(Player player, String[] args) {
        if (!permissionChecker.hasPermission(player, FineClaimPermission.COMMAND_UNTRUST)) {
            FineClaimMessages.sendError(player, "You do not have permission to use this command.");
            return;
        }

        if (args.length < 1) {
            FineClaimMessages.sendWarning(player, "Usage: /claim untrust <player>");
            return;
        }

        var location = ClaimLocationMapper.fromLocation(player.getLocation());
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
        UUID targetId = offlinePlayer.getUniqueId();
        ClaimOperationResult result = claimService.untrustPlayer(
                location.worldName(),
                location.x(),
                location.y(),
                location.z(),
                player.getUniqueId(),
                targetId
        );
        ClaimCommand.sendOperationResult(player, result);
    }
}
