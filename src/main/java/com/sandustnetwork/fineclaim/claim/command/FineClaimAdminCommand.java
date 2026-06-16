package com.sandustnetwork.fineclaim.claim.command;

import com.sandustnetwork.fineclaim.claim.config.ClaimLimitChecker;
import com.sandustnetwork.fineclaim.claim.config.ClaimSettings;
import com.sandustnetwork.fineclaim.claim.storage.file.FileClaimRepository;
import com.sandustnetwork.fineclaim.claim.util.FineClaimMessages;
import com.sandustnetwork.fineclaim.claim.visual.ClaimPreviewManager;
import com.sandustnetwork.fineclaim.permission.FineClaimPermission;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class FineClaimAdminCommand implements BasicCommand {

    private final JavaPlugin plugin;
    private final FileClaimRepository claimRepository;
    private final ClaimLimitChecker limitChecker;
    private final ClaimPreviewManager previewManager;

    public FineClaimAdminCommand(
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

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();

        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            FineClaimMessages.sendWarning(sender, "Usage: /fineclaim reload");
            return;
        }

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

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length == 1) {
            return filterSuggestions(List.of("reload"), args[0]);
        }
        return List.of();
    }

    private static List<String> filterSuggestions(List<String> options, String prefix) {
        String normalizedPrefix = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(normalizedPrefix)) {
                matches.add(option);
            }
        }
        return matches;
    }
}
