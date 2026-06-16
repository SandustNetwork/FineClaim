package com.sandustnetwork.fineclaim.claim.command;

import com.sandustnetwork.fineclaim.claim.application.ClaimService;
import com.sandustnetwork.fineclaim.claim.domain.Claim;
import com.sandustnetwork.fineclaim.claim.domain.ClaimChunk;
import com.sandustnetwork.fineclaim.claim.util.ClaimChunkMapper;
import com.sandustnetwork.fineclaim.permission.FineClaimPermission;
import com.sandustnetwork.fineclaim.permission.PermissionChecker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Optional;

public final class ClaimInfoCommand implements CommandExecutor {

    private static final String NO_PERMISSION_MESSAGE = "You do not have permission to use this command.";
    private static final String CANNOT_VIEW_MESSAGE = "You cannot view this claim.";

    private final ClaimService claimService;
    private final PermissionChecker permissionChecker;

    public ClaimInfoCommand(ClaimService claimService, PermissionChecker permissionChecker) {
        this.claimService = Objects.requireNonNull(claimService, "claimService");
        this.permissionChecker = Objects.requireNonNull(permissionChecker, "permissionChecker");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!permissionChecker.hasPermission(player, FineClaimPermission.COMMAND_INFO)) {
            player.sendMessage(NO_PERMISSION_MESSAGE);
            return true;
        }

        ClaimChunk chunk = ClaimChunkMapper.fromLocation(player.getLocation());
        Optional<Claim> claimOptional = claimService.getClaimAt(chunk);
        if (claimOptional.isEmpty()) {
            player.sendMessage("This chunk is not claimed.");
            return true;
        }

        Claim claim = claimOptional.get();
        if (!permissionChecker.canViewClaim(player, claim)) {
            player.sendMessage(CANNOT_VIEW_MESSAGE);
            return true;
        }

        ClaimChunk claimChunk = claim.getChunk();
        player.sendMessage("Claim owner: " + claim.getOwner());
        player.sendMessage("Chunk: " + claimChunk.worldName() + " [" + claimChunk.chunkX() + ", " + claimChunk.chunkZ() + "]");
        player.sendMessage("Trusted players: " + claim.getTrustedPlayers().size());
        player.sendMessage("Created at: " + claim.getCreatedAt());
        return true;
    }
}
