package com.sandustnetwork.fineclaim;

import com.sandustnetwork.fineclaim.claim.application.ClaimService;
import com.sandustnetwork.fineclaim.claim.command.ClaimCommand;
import com.sandustnetwork.fineclaim.claim.command.ClaimInfoCommand;
import com.sandustnetwork.fineclaim.claim.command.FineClaimAdminCommand;
import com.sandustnetwork.fineclaim.claim.command.TrustCommand;
import com.sandustnetwork.fineclaim.claim.command.UnclaimCommand;
import com.sandustnetwork.fineclaim.claim.command.UntrustCommand;
import com.sandustnetwork.fineclaim.claim.config.ClaimLimitChecker;
import com.sandustnetwork.fineclaim.claim.config.ClaimSettings;
import com.sandustnetwork.fineclaim.claim.protection.ClaimProtectionListener;
import com.sandustnetwork.fineclaim.claim.protection.ProtectionCheck;
import com.sandustnetwork.fineclaim.claim.storage.file.FileClaimRepository;
import com.sandustnetwork.fineclaim.claim.visual.ChunkBorderDisplay;
import com.sandustnetwork.fineclaim.claim.visual.ClaimPreviewManager;
import com.sandustnetwork.fineclaim.permission.PermissionChecker;
import org.bukkit.plugin.java.JavaPlugin;

public final class FineClaimPlugin extends JavaPlugin {

    private FileClaimRepository claimRepository;
    private ClaimLimitChecker limitChecker;
    private ClaimPreviewManager previewManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        ClaimSettings settings;
        try {
            settings = ClaimSettings.fromPlugin(this);
        } catch (RuntimeException exception) {
            getLogger().severe("Failed to load config: " + exception.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            claimRepository = new FileClaimRepository(getDataFolder().toPath(), getLogger());
        } catch (RuntimeException exception) {
            getLogger().severe("Failed to initialize claim storage: " + exception.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        limitChecker = new ClaimLimitChecker(claimRepository, settings);
        ClaimService claimService = new ClaimService(claimRepository, limitChecker);
        PermissionChecker permissionChecker = new PermissionChecker();
        ChunkBorderDisplay borderDisplay = new ChunkBorderDisplay(this);
        previewManager = new ClaimPreviewManager(this, borderDisplay, settings);

        registerCommand("claim", "Claim and manage your land", new ClaimCommand(claimService, permissionChecker, previewManager));
        registerCommand("unclaim", "Remove your claim from the current chunk", new UnclaimCommand(claimService, permissionChecker));
        registerCommand("trust", "Trust a player to build in your current chunk claim", new TrustCommand(claimService, permissionChecker));
        registerCommand("untrust", "Remove build trust from a player in your current chunk claim", new UntrustCommand(claimService, permissionChecker));
        registerCommand("claiminfo", "Show claim information for the current chunk", new ClaimInfoCommand(claimService, permissionChecker, previewManager));
        registerCommand("fineclaim", "FineClaim admin commands", new FineClaimAdminCommand(this, claimRepository, limitChecker, previewManager));

        ProtectionCheck protectionCheck = new ProtectionCheck(claimService, permissionChecker);
        getServer().getPluginManager().registerEvents(new ClaimProtectionListener(protectionCheck), this);

        getLogger().info("FineClaim enabled");
    }

    @Override
    public void onDisable() {
        if (previewManager != null) {
            previewManager.cleanupAll();
        }
        getLogger().info("FineClaim disabled");
    }
}
