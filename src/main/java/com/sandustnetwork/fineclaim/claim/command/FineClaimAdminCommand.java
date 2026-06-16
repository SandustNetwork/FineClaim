package com.sandustnetwork.fineclaim.claim.command;

import com.sandustnetwork.fineclaim.claim.storage.file.FileClaimRepository;
import com.sandustnetwork.fineclaim.claim.util.FineClaimMessages;
import com.sandustnetwork.fineclaim.permission.FineClaimPermission;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class FineClaimAdminCommand implements BasicCommand {

    private final JavaPlugin plugin;
    private final FileClaimRepository claimRepository;

    public FineClaimAdminCommand(JavaPlugin plugin, FileClaimRepository claimRepository) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.claimRepository = Objects.requireNonNull(claimRepository, "claimRepository");
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
        int claimCount = claimRepository.reloadFromFile();
        FineClaimMessages.sendSuccess(
                sender,
                "Reloaded config.yml and loaded " + claimCount + " claim(s) from claims.yml."
        );
    }
}
