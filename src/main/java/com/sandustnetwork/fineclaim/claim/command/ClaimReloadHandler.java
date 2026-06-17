package com.sandustnetwork.fineclaim.claim.command;

import com.sandustnetwork.fineclaim.claim.config.ClaimLimitChecker;
import com.sandustnetwork.fineclaim.claim.config.ClaimSettings;
import com.sandustnetwork.fineclaim.claim.storage.file.FileClaimRepository;
import com.sandustnetwork.fineclaim.claim.util.FineClaimMessages;
import com.sandustnetwork.fineclaim.claim.visual.ClaimPreviewManager;
import com.sandustnetwork.fineclaim.permission.FineClaimPermission;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

final class ClaimReloadHandler {

    private final JavaPlugin plugin;
    private final FileClaimRepository claimRepository;
    private final ClaimLimitChecker limitChecker;
    private final ClaimPreviewManager previewManager;

    ClaimReloadHandler(
            JavaPlugin plugin,
            FileClaimRepository claimRepository,
            ClaimLimitChecker limitChecker,
            ClaimPreviewManager previewManager
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.claimRepository = Objects.requireNonNull(claimRepository, "claimRepository");
        this.limitChecker = Objects.requireNonNull(limitChecker, "limitChecker");
        this.previewManager = Objects.requireNonNull(previewManager, "previewManager");
    }

    void handle(CommandSender sender) {
        if (!sender.hasPermission(FineClaimPermission.ADMIN_RELOAD)) {
            FineClaimMessages.sendError(sender, "You do not have permission to use this command.");
            return;
        }

        plugin.reloadConfig();
        ClaimSettings settings = ClaimSettings.fromPlugin(plugin);
        limitChecker.updateSettings(settings);
        previewManager.updateSettings(settings);

        int blockCount = claimRepository.reloadFromFile();
        FineClaimMessages.sendSuccess(
                sender,
                "Reloaded config.yml and loaded " + blockCount + " block(s) from claims.yml."
        );
    }
}
