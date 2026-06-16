package com.sandustnetwork.fineclaim.claim.command;

import com.sandustnetwork.fineclaim.claim.application.ClaimOperationResult;
import com.sandustnetwork.fineclaim.claim.application.ClaimService;
import com.sandustnetwork.fineclaim.claim.util.ClaimLocationMapper;
import com.sandustnetwork.fineclaim.claim.util.FineClaimMessages;
import com.sandustnetwork.fineclaim.permission.FineClaimPermission;
import com.sandustnetwork.fineclaim.permission.PermissionChecker;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public final class TrustCommand implements BasicCommand {

    private final ClaimService claimService;
    private final PermissionChecker permissionChecker;

    public TrustCommand(ClaimService claimService, PermissionChecker permissionChecker) {
        this.claimService = Objects.requireNonNull(claimService, "claimService");
        this.permissionChecker = Objects.requireNonNull(permissionChecker, "permissionChecker");
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();
        if (!(sender instanceof Player player)) {
            FineClaimMessages.sendError(sender, "This command can only be used by players.");
            return;
        }

        if (!permissionChecker.hasPermission(player, FineClaimPermission.COMMAND_TRUST)) {
            FineClaimMessages.sendError(player, "You do not have permission to use this command.");
            return;
        }

        if (args.length < 1) {
            FineClaimMessages.sendWarning(player, "Usage: /trust <player>");
            return;
        }

        var location = ClaimLocationMapper.fromLocation(player.getLocation());
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
        UUID targetId = offlinePlayer.getUniqueId();
        ClaimOperationResult result = claimService.trustPlayer(
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
