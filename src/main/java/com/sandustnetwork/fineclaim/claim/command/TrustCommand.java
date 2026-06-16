package com.sandustnetwork.fineclaim.claim.command;

import com.sandustnetwork.fineclaim.claim.application.ClaimOperationResult;
import com.sandustnetwork.fineclaim.claim.application.ClaimService;
import com.sandustnetwork.fineclaim.claim.domain.ClaimChunk;
import com.sandustnetwork.fineclaim.claim.util.ClaimChunkMapper;
import com.sandustnetwork.fineclaim.permission.FineClaimPermission;
import com.sandustnetwork.fineclaim.permission.PermissionChecker;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public final class TrustCommand implements CommandExecutor {

    private static final String NO_PERMISSION_MESSAGE = "You do not have permission to use this command.";

    private final ClaimService claimService;
    private final PermissionChecker permissionChecker;

    public TrustCommand(ClaimService claimService, PermissionChecker permissionChecker) {
        this.claimService = Objects.requireNonNull(claimService, "claimService");
        this.permissionChecker = Objects.requireNonNull(permissionChecker, "permissionChecker");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!permissionChecker.hasPermission(player, FineClaimPermission.COMMAND_TRUST)) {
            player.sendMessage(NO_PERMISSION_MESSAGE);
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("Usage: /trust <player>");
            return true;
        }

        ClaimChunk chunk = ClaimChunkMapper.fromLocation(player.getLocation());
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
        UUID targetId = offlinePlayer.getUniqueId();
        ClaimOperationResult result = claimService.trustPlayer(chunk, player.getUniqueId(), targetId);
        player.sendMessage(result.message());
        return true;
    }
}
