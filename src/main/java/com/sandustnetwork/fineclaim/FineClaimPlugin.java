package com.sandustnetwork.fineclaim;

import com.sandustnetwork.fineclaim.claim.application.ClaimService;
import com.sandustnetwork.fineclaim.claim.command.ClaimCommand;
import com.sandustnetwork.fineclaim.claim.command.ClaimInfoCommand;
import com.sandustnetwork.fineclaim.claim.command.FineClaimAdminCommand;
import com.sandustnetwork.fineclaim.claim.command.TrustCommand;
import com.sandustnetwork.fineclaim.claim.command.UnclaimCommand;
import com.sandustnetwork.fineclaim.claim.command.UntrustCommand;
import com.sandustnetwork.fineclaim.claim.protection.ClaimProtectionListener;
import com.sandustnetwork.fineclaim.claim.protection.ProtectionCheck;
import com.sandustnetwork.fineclaim.claim.storage.file.FileClaimRepository;
import com.sandustnetwork.fineclaim.permission.PermissionChecker;
import org.bukkit.plugin.java.JavaPlugin;

public final class FineClaimPlugin extends JavaPlugin {

    private ClaimService claimService;
    private FileClaimRepository claimRepository;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        try {
            claimRepository = new FileClaimRepository(getDataFolder().toPath(), getLogger());
        } catch (RuntimeException exception) {
            getLogger().severe("Failed to initialize claim storage: " + exception.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        claimService = new ClaimService(claimRepository);
        PermissionChecker permissionChecker = new PermissionChecker();

        registerCommand("claim", "Claim the chunk you are standing in", new ClaimCommand(claimService, permissionChecker));
        registerCommand("unclaim", "Remove your claim from the current chunk", new UnclaimCommand(claimService, permissionChecker));
        registerCommand("trust", "Trust a player to build in your current chunk claim", new TrustCommand(claimService, permissionChecker));
        registerCommand("untrust", "Remove build trust from a player in your current chunk claim", new UntrustCommand(claimService, permissionChecker));
        registerCommand("claiminfo", "Show claim information for the current chunk", new ClaimInfoCommand(claimService, permissionChecker));
        registerCommand("fineclaim", "FineClaim admin commands", new FineClaimAdminCommand(this, claimRepository));

        ProtectionCheck protectionCheck = new ProtectionCheck(claimService, permissionChecker);
        getServer().getPluginManager().registerEvents(new ClaimProtectionListener(protectionCheck), this);

        getLogger().info("FineClaim enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("FineClaim disabled");
    }
}
