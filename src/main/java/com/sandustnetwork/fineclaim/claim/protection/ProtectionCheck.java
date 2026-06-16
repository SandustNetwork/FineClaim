package com.sandustnetwork.fineclaim.claim.protection;

import com.sandustnetwork.fineclaim.claim.application.ClaimService;
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

    public ProtectionResult checkBuild(String worldName, int x, int y, int z, Player player) {
        Objects.requireNonNull(worldName, "worldName");
        Objects.requireNonNull(player, "player");
        return evaluateAccess(worldName, x, y, z, player);
    }

    public ProtectionResult checkInteract(String worldName, int x, int y, int z, Player player) {
        Objects.requireNonNull(worldName, "worldName");
        Objects.requireNonNull(player, "player");
        return evaluateAccess(worldName, x, y, z, player);
    }

    private ProtectionResult evaluateAccess(String worldName, int x, int y, int z, Player player) {
        if (permissionChecker.hasAdminBypass(player)) {
            return ProtectionResult.allowed();
        }

        UUID playerId = player.getUniqueId();
        if (claimService.getClaimAtBlock(worldName, x, y, z).isEmpty()) {
            return ProtectionResult.allowed();
        }
        if (claimService.canBuild(worldName, x, y, z, playerId)) {
            return ProtectionResult.allowed();
        }
        return ProtectionResult.denied("You cannot do that in this claim.");
    }
}
