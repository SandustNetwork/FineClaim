package com.sandustnetwork.fineclaim.claim.command;

import com.sandustnetwork.fineclaim.claim.application.ClaimService;
import com.sandustnetwork.fineclaim.claim.domain.Claim;
import com.sandustnetwork.fineclaim.claim.domain.ClaimBox;
import com.sandustnetwork.fineclaim.claim.util.ClaimLocationMapper;
import com.sandustnetwork.fineclaim.claim.util.FineClaimMessages;
import com.sandustnetwork.fineclaim.claim.visual.ClaimPreviewManager;
import com.sandustnetwork.fineclaim.permission.FineClaimPermission;
import com.sandustnetwork.fineclaim.permission.PermissionChecker;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Optional;

final class ClaimInfoHandler {

    private final ClaimService claimService;
    private final PermissionChecker permissionChecker;
    private final ClaimPreviewManager previewManager;

    ClaimInfoHandler(
            ClaimService claimService,
            PermissionChecker permissionChecker,
            ClaimPreviewManager previewManager
    ) {
        this.claimService = Objects.requireNonNull(claimService, "claimService");
        this.permissionChecker = Objects.requireNonNull(permissionChecker, "permissionChecker");
        this.previewManager = Objects.requireNonNull(previewManager, "previewManager");
    }

    void handle(Player player) {
        if (!permissionChecker.hasPermission(player, FineClaimPermission.COMMAND_INFO)) {
            FineClaimMessages.sendError(player, "You do not have permission to use this command.");
            return;
        }

        var location = ClaimLocationMapper.fromLocation(player.getLocation());
        Optional<Claim> claimOptional = claimService.getClaimAtBlock(
                location.worldName(),
                location.x(),
                location.y(),
                location.z()
        );
        if (claimOptional.isEmpty()) {
            FineClaimMessages.sendInfo(player, "This location is not claimed.");
            return;
        }

        Claim claim = claimOptional.get();
        if (!permissionChecker.canViewClaim(player, claim)) {
            FineClaimMessages.sendError(player, "You cannot view this claim.");
            return;
        }

        ClaimBox box = claim.getBox();
        FineClaimMessages.sendClaimInfoPanel(
                player,
                FineClaimMessages.resolvePlayerName(claim.getOwner()),
                box,
                claim.getTrustedPlayers().size(),
                claim.getCreatedAt()
        );
        previewManager.showClaimBorder(player, claim);
    }
}
