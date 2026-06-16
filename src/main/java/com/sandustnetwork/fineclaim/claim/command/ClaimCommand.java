package com.sandustnetwork.fineclaim.claim.command;

import com.sandustnetwork.fineclaim.claim.application.ClaimOperationResult;
import com.sandustnetwork.fineclaim.claim.application.ClaimService;
import com.sandustnetwork.fineclaim.claim.domain.ClaimChunk;
import com.sandustnetwork.fineclaim.claim.util.ClaimChunkMapper;
import com.sandustnetwork.fineclaim.permission.FineClaimPermission;
import com.sandustnetwork.fineclaim.permission.PermissionChecker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public final class ClaimCommand implements CommandExecutor {

    private static final String NO_PERMISSION_MESSAGE = "You do not have permission to use this command.";

    private final ClaimService claimService;
    private final PermissionChecker permissionChecker;

    public ClaimCommand(ClaimService claimService, PermissionChecker permissionChecker) {
        this.claimService = Objects.requireNonNull(claimService, "claimService");
        this.permissionChecker = Objects.requireNonNull(permissionChecker, "permissionChecker");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!permissionChecker.hasPermission(player, FineClaimPermission.COMMAND_CLAIM)) {
            player.sendMessage(NO_PERMISSION_MESSAGE);
            return true;
        }

        ClaimChunk chunk = ClaimChunkMapper.fromLocation(player.getLocation());
        ClaimOperationResult result = claimService.createClaim(chunk, player.getUniqueId());
        player.sendMessage(result.message());
        return true;
    }
}
