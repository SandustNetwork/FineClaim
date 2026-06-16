package com.sandustnetwork.fineclaim.claim.command;

import com.sandustnetwork.fineclaim.claim.application.ClaimOperationResult;
import com.sandustnetwork.fineclaim.claim.application.ClaimService;
import com.sandustnetwork.fineclaim.claim.domain.Claim;
import com.sandustnetwork.fineclaim.claim.domain.ClaimChunk;
import com.sandustnetwork.fineclaim.claim.util.ClaimChunkMapper;
import com.sandustnetwork.fineclaim.claim.util.FineClaimMessages;
import com.sandustnetwork.fineclaim.claim.visual.ClaimPreviewManager;
import com.sandustnetwork.fineclaim.permission.FineClaimPermission;
import com.sandustnetwork.fineclaim.permission.PermissionChecker;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Optional;

public final class ClaimCommand implements BasicCommand {

    private final ClaimService claimService;
    private final PermissionChecker permissionChecker;
    private final ClaimPreviewManager previewManager;

    public ClaimCommand(
            ClaimService claimService,
            PermissionChecker permissionChecker,
            ClaimPreviewManager previewManager
    ) {
        this.claimService = Objects.requireNonNull(claimService, "claimService");
        this.permissionChecker = Objects.requireNonNull(permissionChecker, "permissionChecker");
        this.previewManager = Objects.requireNonNull(previewManager, "previewManager");
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();
        if (!(sender instanceof Player player)) {
            FineClaimMessages.sendError(sender, "This command can only be used by players.");
            return;
        }

        if (!permissionChecker.hasPermission(player, FineClaimPermission.COMMAND_CLAIM)) {
            FineClaimMessages.sendError(player, "You do not have permission to use this command.");
            return;
        }

        if (args.length == 0) {
            handlePreview(player);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "confirm" -> handleConfirm(player);
            case "cancel" -> handleCancel(player);
            case "expand" -> handleExpand(player);
            case "shrink" -> handleShrink(player);
            default -> FineClaimMessages.sendWarning(
                    player,
                    "Usage: /claim, /claim confirm, /claim cancel, /claim expand, /claim shrink"
            );
        }
    }

    private void handlePreview(Player player) {
        ClaimChunk chunk = ClaimChunkMapper.fromLocation(player.getLocation());
        if (claimService.getClaimAt(chunk).isPresent()) {
            FineClaimMessages.sendError(player, "This chunk is already claimed.");
            return;
        }

        previewManager.startPreview(player, chunk);
    }

    private void handleConfirm(Player player) {
        Optional<ClaimChunk> previewChunk = previewManager.getPreviewChunk(player);
        if (previewChunk.isEmpty()) {
            FineClaimMessages.sendError(player, "You do not have an active claim preview.");
            return;
        }

        ClaimOperationResult result = claimService.createRegion(previewChunk.get(), player.getUniqueId());
        if (result.success()) {
            previewManager.cancelPreview(player);
            claimService.getClaimAt(previewChunk.get()).ifPresent(claim -> previewManager.showRegionBorder(player, claim));
        }
        sendOperationResult(player, result);
    }

    private void handleCancel(Player player) {
        if (previewManager.getPreviewChunk(player).isEmpty()) {
            FineClaimMessages.sendWarning(player, "You do not have an active claim preview.");
            return;
        }

        previewManager.cancelPreview(player);
        FineClaimMessages.sendInfo(player, "Claim preview cancelled.");
    }

    private void handleExpand(Player player) {
        ClaimChunk chunk = ClaimChunkMapper.fromLocation(player.getLocation());
        ClaimOperationResult result = claimService.expandRegion(chunk, player.getUniqueId());
        if (result.success()) {
            claimService.getClaimAt(chunk).ifPresent(claim -> previewManager.showRegionBorder(player, claim));
        }
        sendOperationResult(player, result);
    }

    private void handleShrink(Player player) {
        ClaimChunk chunk = ClaimChunkMapper.fromLocation(player.getLocation());
        ClaimOperationResult result = claimService.shrinkRegion(chunk, player.getUniqueId());
        if (result.success()) {
            Optional<Claim> remainingClaim = claimService.getClaimAt(chunk);
            if (remainingClaim.isPresent()) {
                previewManager.showRegionBorder(player, remainingClaim.get());
            } else {
                previewManager.cancelPreview(player);
            }
        }
        sendOperationResult(player, result);
    }

    static void sendOperationResult(Player player, ClaimOperationResult result) {
        if (result.success()) {
            FineClaimMessages.sendSuccess(player, result.message());
        } else {
            FineClaimMessages.sendError(player, result.message());
        }
    }
}
