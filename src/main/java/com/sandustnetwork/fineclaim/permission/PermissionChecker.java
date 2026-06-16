package com.sandustnetwork.fineclaim.permission;

import com.sandustnetwork.fineclaim.claim.domain.Claim;
import org.bukkit.entity.Player;

import java.util.Objects;

public final class PermissionChecker {

    public boolean hasPermission(Player player, String permission) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(permission, "permission");
        return player.hasPermission(permission);
    }

    public boolean hasAdminBypass(Player player) {
        return hasPermission(player, FineClaimPermission.ADMIN_BYPASS);
    }

    public boolean hasAdminInfo(Player player) {
        return hasPermission(player, FineClaimPermission.ADMIN_INFO);
    }

    public boolean canViewClaim(Player player, Claim claim) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(claim, "claim");

        if (claim.isOwner(player.getUniqueId()) || claim.isTrusted(player.getUniqueId())) {
            return true;
        }

        return hasAdminInfo(player);
    }
}
