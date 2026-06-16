package com.sandustnetwork.fineclaim.claim.protection;

import com.sandustnetwork.fineclaim.claim.application.ClaimService;
import com.sandustnetwork.fineclaim.claim.domain.ClaimChunk;
import com.sandustnetwork.fineclaim.permission.PermissionChecker;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public final class ProtectionCheck {

    private final ClaimService claimService;
    private final PermissionChecker permissionChecker;

    public ProtectionCheck(ClaimService claimService, PermissionChecker permissionChecker) {
        this.claimService = Objects.requireNonNull(claimService, "claimService");
        this.permissionChecker = Objects.requireNonNull(permissionChecker, "permissionChecker");
    }

    public ProtectionResult checkBuild(ClaimChunk chunk, Player player) {
        Objects.requireNonNull(chunk, "chunk");
        Objects.requireNonNull(player, "player");
        return evaluateAccess(chunk, player);
    }

    public ProtectionResult checkInteract(ClaimChunk chunk, Player player) {
        Objects.requireNonNull(chunk, "chunk");
        Objects.requireNonNull(player, "player");
        return evaluateAccess(chunk, player);
    }

    private ProtectionResult evaluateAccess(ClaimChunk chunk, Player player) {
        if (permissionChecker.hasAdminBypass(player)) {
            return ProtectionResult.allowed();
        }

        UUID playerId = player.getUniqueId();
        if (claimService.getClaimAt(chunk).isEmpty()) {
            return ProtectionResult.allowed();
        }
        if (claimService.canBuild(chunk, playerId)) {
            return ProtectionResult.allowed();
        }
        return ProtectionResult.denied("You cannot do that in this claim.");
    }
}
