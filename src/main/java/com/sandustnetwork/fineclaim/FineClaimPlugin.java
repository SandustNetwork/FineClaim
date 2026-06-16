package com.sandustnetwork.fineclaim;

import com.sandustnetwork.fineclaim.claim.application.ClaimService;
import com.sandustnetwork.fineclaim.claim.command.ClaimCommand;
import com.sandustnetwork.fineclaim.claim.command.ClaimInfoCommand;
import com.sandustnetwork.fineclaim.claim.command.TrustCommand;
import com.sandustnetwork.fineclaim.claim.command.UnclaimCommand;
import com.sandustnetwork.fineclaim.claim.command.UntrustCommand;
import com.sandustnetwork.fineclaim.claim.protection.ClaimProtectionListener;
import com.sandustnetwork.fineclaim.claim.protection.ProtectionCheck;
import com.sandustnetwork.fineclaim.claim.storage.ClaimRepository;
import com.sandustnetwork.fineclaim.claim.storage.file.FileClaimRepository;
import com.sandustnetwork.fineclaim.permission.PermissionChecker;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class FineClaimPlugin extends JavaPlugin {

    private ClaimService claimService;

    @Override
    public void onEnable() {
        ClaimRepository repository;
        try {
            repository = new FileClaimRepository(getDataFolder().toPath(), getLogger());
        } catch (RuntimeException exception) {
            getLogger().severe("Failed to initialize claim storage: " + exception.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        claimService = new ClaimService(repository);
        PermissionChecker permissionChecker = new PermissionChecker();

        registerCommand("claim", new ClaimCommand(claimService, permissionChecker));
        registerCommand("unclaim", new UnclaimCommand(claimService, permissionChecker));
        registerCommand("trust", new TrustCommand(claimService, permissionChecker));
        registerCommand("untrust", new UntrustCommand(claimService, permissionChecker));
        registerCommand("claiminfo", new ClaimInfoCommand(claimService, permissionChecker));

        ProtectionCheck protectionCheck = new ProtectionCheck(claimService, permissionChecker);
        getServer().getPluginManager().registerEvents(new ClaimProtectionListener(protectionCheck), this);

        getLogger().info("FineClaim enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("FineClaim disabled");
    }

    private void registerCommand(String name, CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            String message = "Command '" + name + "' is not defined in paper-plugin.yml";
            getLogger().severe(message);
            throw new IllegalStateException(message);
        }
        command.setExecutor(executor);
    }
}
