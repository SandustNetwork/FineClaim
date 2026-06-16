package com.sandustnetwork.fineclaim.claim.command;

import com.sandustnetwork.fineclaim.claim.application.ClaimService;
import com.sandustnetwork.fineclaim.claim.domain.Claim;
import com.sandustnetwork.fineclaim.claim.domain.ClaimChunk;
import com.sandustnetwork.fineclaim.claim.util.ClaimChunkMapper;
import com.sandustnetwork.fineclaim.claim.util.FineClaimMessages;
import com.sandustnetwork.fineclaim.permission.FineClaimPermission;
import com.sandustnetwork.fineclaim.permission.PermissionChecker;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class ClaimInfoCommand implements BasicCommand {

    private final ClaimService claimService;
    private final PermissionChecker permissionChecker;

    public ClaimInfoCommand(ClaimService claimService, PermissionChecker permissionChecker) {
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

        if (!permissionChecker.hasPermission(player, FineClaimPermission.COMMAND_INFO)) {
            FineClaimMessages.sendError(player, "You do not have permission to use this command.");
            return;
        }

        ClaimChunk chunk = ClaimChunkMapper.fromLocation(player.getLocation());
        Optional<Claim> claimOptional = claimService.getClaimAt(chunk);
        if (claimOptional.isEmpty()) {
            FineClaimMessages.sendInfo(player, "This chunk is not claimed.");
            return;
        }

        Claim claim = claimOptional.get();
        if (!permissionChecker.canViewClaim(player, claim)) {
            FineClaimMessages.sendError(player, "You cannot view this claim.");
            return;
        }

        UUID ownerId = claim.getOwner();
        ClaimChunk claimChunk = claim.getChunk();
        String chunkCoordinates = claimChunk.worldName() + " [" + claimChunk.chunkX() + ", " + claimChunk.chunkZ() + "]";

        FineClaimMessages.sendSectionHeader(player, "Claim Info");
        FineClaimMessages.sendLabeled(player, "Owner", FineClaimMessages.resolvePlayerName(ownerId));
        FineClaimMessages.sendLabeled(player, "Owner ID", FineClaimMessages.formatPlayerId(ownerId));
        FineClaimMessages.sendLabeled(player, "Chunk", chunkCoordinates);
        FineClaimMessages.sendLabeled(player, "Trusted players", String.valueOf(claim.getTrustedPlayers().size()));
        FineClaimMessages.sendLabeled(player, "Created", FineClaimMessages.formatCreatedAt(claim.getCreatedAt()));
    }
}
