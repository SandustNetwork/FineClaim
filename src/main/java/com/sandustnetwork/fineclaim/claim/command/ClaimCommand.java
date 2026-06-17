package com.sandustnetwork.fineclaim.claim.command;

import com.sandustnetwork.fineclaim.claim.application.ClaimOperationResult;
import com.sandustnetwork.fineclaim.claim.application.ClaimService;
import com.sandustnetwork.fineclaim.claim.config.ClaimLimitChecker;
import com.sandustnetwork.fineclaim.claim.domain.Claim;
import com.sandustnetwork.fineclaim.claim.domain.ClaimBox;
import com.sandustnetwork.fineclaim.claim.storage.file.FileClaimRepository;
import com.sandustnetwork.fineclaim.claim.util.ClaimLocationMapper;
import com.sandustnetwork.fineclaim.claim.util.FineClaimMessages;
import com.sandustnetwork.fineclaim.claim.visual.ClaimPreviewManager;
import com.sandustnetwork.fineclaim.claim.wand.ClaimWandManager;
import com.sandustnetwork.fineclaim.permission.FineClaimPermission;
import com.sandustnetwork.fineclaim.permission.PermissionChecker;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class ClaimCommand implements BasicCommand {

    private static final List<String> ALL_SUBCOMMANDS = List.of(
            "confirm", "cancel", "resize", "info", "trust", "untrust", "unclaim", "reload"
    );
    private static final List<String> RESIZE_SUBCOMMANDS = List.of("confirm", "cancel");
    private static final List<String> PLAYER_ARG_SUBCOMMANDS = List.of("trust", "untrust");
    private static final String USAGE = "Usage: /claim, /claim confirm, /claim cancel, /claim resize [confirm|cancel], "
            + "/claim info, /claim trust <player>, /claim untrust <player>, /claim unclaim, /claim reload";

    private final ClaimService claimService;
    private final PermissionChecker permissionChecker;
    private final ClaimWandManager wandManager;
    private final ClaimInfoHandler infoHandler;
    private final ClaimTrustHandler trustHandler;
    private final ClaimUntrustHandler untrustHandler;
    private final ClaimUnclaimHandler unclaimHandler;
    private final ClaimReloadHandler reloadHandler;

    public ClaimCommand(
            JavaPlugin plugin,
            ClaimService claimService,
            PermissionChecker permissionChecker,
            ClaimWandManager wandManager,
            FileClaimRepository claimRepository,
            ClaimLimitChecker limitChecker,
            ClaimPreviewManager previewManager
    ) {
        this.claimService = Objects.requireNonNull(claimService, "claimService");
        this.permissionChecker = Objects.requireNonNull(permissionChecker, "permissionChecker");
        this.wandManager = Objects.requireNonNull(wandManager, "wandManager");
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(claimRepository, "claimRepository");
        Objects.requireNonNull(limitChecker, "limitChecker");
        Objects.requireNonNull(previewManager, "previewManager");
        this.infoHandler = new ClaimInfoHandler(claimService, permissionChecker, previewManager);
        this.trustHandler = new ClaimTrustHandler(claimService, permissionChecker);
        this.untrustHandler = new ClaimUntrustHandler(claimService, permissionChecker);
        this.unclaimHandler = new ClaimUnclaimHandler(claimService, permissionChecker);
        this.reloadHandler = new ClaimReloadHandler(plugin, claimRepository, limitChecker, previewManager);
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                FineClaimMessages.sendError(sender, "This command can only be used by players.");
                return;
            }
            if (!permissionChecker.hasPermission(player, FineClaimPermission.COMMAND_CLAIM)) {
                FineClaimMessages.sendError(player, "You do not have permission to use this command.");
                return;
            }
            wandManager.startCreate(player);
            return;
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);
        switch (subcommand) {
            case "confirm" -> requireClaimPlayer(sender, player -> handleConfirm(player));
            case "cancel" -> requireClaimPlayer(sender, player -> handleCancel(player));
            case "resize" -> requireClaimPlayer(sender, player -> handleResize(player, args));
            case "info" -> requirePlayer(sender, infoHandler::handle);
            case "trust" -> requirePlayer(sender, player -> trustHandler.handle(player, tailArgs(args)));
            case "untrust" -> requirePlayer(sender, player -> untrustHandler.handle(player, tailArgs(args)));
            case "unclaim" -> requirePlayer(sender, unclaimHandler::handle);
            case "reload" -> reloadHandler.handle(sender);
            default -> FineClaimMessages.sendWarning(sender, USAGE);
        }
    }

    @Override
    public @Nullable String permission() {
        return null;
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();

        if (args.length == 0) {
            return permittedSubcommands(sender, null);
        }
        if (args.length == 1) {
            return CommandSuggestions.filter(permittedSubcommands(sender, asPlayer(sender)), args[0]);
        }
        if (args.length >= 2 && args[0].equalsIgnoreCase("resize")) {
            if (!sender.hasPermission(FineClaimPermission.COMMAND_CLAIM)) {
                return List.of();
            }
            return CommandSuggestions.filter(RESIZE_SUBCOMMANDS, args[args.length - 1]);
        }
        if (args.length == 2 && isPlayerArgSubcommand(args[0])) {
            if (!(sender instanceof Player player)) {
                return List.of();
            }
            if (!hasPermissionForSubcommand(player, args[0])) {
                return List.of();
            }
            return CommandSuggestions.filter(onlinePlayerNames(), args[1]);
        }
        return List.of();
    }

    private void requirePlayer(CommandSender sender, PlayerAction action) {
        if (!(sender instanceof Player player)) {
            FineClaimMessages.sendError(sender, "This command can only be used by players.");
            return;
        }
        action.run(player);
    }

    private void requireClaimPlayer(CommandSender sender, PlayerAction action) {
        if (!(sender instanceof Player player)) {
            FineClaimMessages.sendError(sender, "This command can only be used by players.");
            return;
        }
        if (!permissionChecker.hasPermission(player, FineClaimPermission.COMMAND_CLAIM)) {
            FineClaimMessages.sendError(player, "You do not have permission to use this command.");
            return;
        }
        action.run(player);
    }

    private List<String> permittedSubcommands(CommandSender sender, @Nullable Player player) {
        List<String> suggestions = new ArrayList<>();
        if (player != null) {
            if (permissionChecker.hasPermission(player, FineClaimPermission.COMMAND_CLAIM)) {
                if (wandManager.getSession(player).isPresent()) {
                    suggestions.add("confirm");
                    suggestions.add("cancel");
                }
                if (isStandingInOwnedClaim(player)) {
                    suggestions.add("resize");
                }
            }
            if (permissionChecker.hasPermission(player, FineClaimPermission.COMMAND_INFO)) {
                suggestions.add("info");
            }
            if (permissionChecker.hasPermission(player, FineClaimPermission.COMMAND_TRUST)) {
                suggestions.add("trust");
            }
            if (permissionChecker.hasPermission(player, FineClaimPermission.COMMAND_UNTRUST)) {
                suggestions.add("untrust");
            }
            if (permissionChecker.hasPermission(player, FineClaimPermission.COMMAND_UNCLAIM)) {
                suggestions.add("unclaim");
            }
        }
        if (sender.hasPermission(FineClaimPermission.ADMIN_RELOAD)) {
            suggestions.add("reload");
        }
        if (suggestions.isEmpty()) {
            return ALL_SUBCOMMANDS;
        }
        for (String option : ALL_SUBCOMMANDS) {
            if (!suggestions.contains(option)) {
                suggestions.add(option);
            }
        }
        return suggestions;
    }

    private boolean hasPermissionForSubcommand(Player player, String subcommand) {
        return switch (subcommand.toLowerCase(Locale.ROOT)) {
            case "trust" -> permissionChecker.hasPermission(player, FineClaimPermission.COMMAND_TRUST);
            case "untrust" -> permissionChecker.hasPermission(player, FineClaimPermission.COMMAND_UNTRUST);
            default -> false;
        };
    }

    private static boolean isPlayerArgSubcommand(String subcommand) {
        return PLAYER_ARG_SUBCOMMANDS.contains(subcommand.toLowerCase(Locale.ROOT));
    }

    private static @Nullable Player asPlayer(CommandSender sender) {
        if (sender instanceof Player player) {
            return player;
        }
        return null;
    }

    private static List<String> onlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .toList();
    }

    private static String[] tailArgs(String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }

    private boolean isStandingInOwnedClaim(Player player) {
        var location = ClaimLocationMapper.fromLocation(player.getLocation());
        return claimService.getClaimAtBlock(
                location.worldName(),
                location.x(),
                location.y(),
                location.z()
        ).map(claim -> claim.isOwner(player.getUniqueId())).orElse(false);
    }

    private void handleConfirm(Player player) {
        Optional<ClaimWandManager.WandSession> session = wandManager.getSession(player);
        if (session.isEmpty() || session.get().mode() != ClaimWandManager.Mode.CREATE) {
            FineClaimMessages.sendError(player, "You do not have an active claim selection.");
            return;
        }

        Optional<ClaimBox> selectedBox = wandManager.getSelectedBox(player);
        if (selectedBox.isEmpty()) {
            FineClaimMessages.sendError(player, "Set both point A and point B before confirming.");
            return;
        }

        ClaimOperationResult result = claimService.createFromBox(selectedBox.get(), player.getUniqueId());
        if (result.success()) {
            wandManager.endSession(player);
        }
        sendOperationResult(player, result);
    }

    private void handleCancel(Player player) {
        if (wandManager.getSession(player).isEmpty()) {
            FineClaimMessages.sendWarning(player, "You do not have an active claim selection.");
            return;
        }

        wandManager.endSession(player);
        FineClaimMessages.sendCancelled(player, "Claim selection cancelled.");
    }

    private void handleResize(Player player, String[] args) {
        if (args.length == 1) {
            var location = ClaimLocationMapper.fromLocation(player.getLocation());
            Optional<Claim> claimOptional = claimService.getClaimAtBlock(
                    location.worldName(),
                    location.x(),
                    location.y(),
                    location.z()
            );
            if (claimOptional.isEmpty()) {
                FineClaimMessages.sendError(player, "No claim exists at your location.");
                return;
            }

            Claim claim = claimOptional.get();
            if (!claim.isOwner(player.getUniqueId())) {
                FineClaimMessages.sendError(player, "Only the claim owner can resize this claim.");
                return;
            }

            wandManager.startResize(player, claim.getId());
            return;
        }

        if (args.length == 2 && args[1].equalsIgnoreCase("confirm")) {
            handleResizeConfirm(player);
            return;
        }

        if (args.length == 2 && args[1].equalsIgnoreCase("cancel")) {
            handleResizeCancel(player);
            return;
        }

        FineClaimMessages.sendWarning(player, "Usage: /claim resize, /claim resize confirm, /claim resize cancel");
    }

    private void handleResizeConfirm(Player player) {
        Optional<ClaimWandManager.WandSession> session = wandManager.getSession(player);
        if (session.isEmpty() || session.get().mode() != ClaimWandManager.Mode.RESIZE) {
            FineClaimMessages.sendError(player, "You do not have an active claim resize.");
            return;
        }

        Optional<ClaimBox> selectedBox = wandManager.getSelectedBox(player);
        if (selectedBox.isEmpty()) {
            FineClaimMessages.sendError(player, "Set both point A and point B before confirming.");
            return;
        }

        ClaimOperationResult result = claimService.resizeClaim(
                session.get().targetClaimId(),
                selectedBox.get(),
                player.getUniqueId()
        );
        if (result.success()) {
            wandManager.endSession(player);
        }
        sendOperationResult(player, result);
    }

    private void handleResizeCancel(Player player) {
        Optional<ClaimWandManager.WandSession> session = wandManager.getSession(player);
        if (session.isEmpty() || session.get().mode() != ClaimWandManager.Mode.RESIZE) {
            FineClaimMessages.sendWarning(player, "You do not have an active claim resize.");
            return;
        }

        wandManager.endSession(player);
        FineClaimMessages.sendCancelled(player, "Claim resize cancelled.");
    }

    static void sendOperationResult(Player player, ClaimOperationResult result) {
        if (result.success()) {
            FineClaimMessages.sendSuccess(player, result.message());
        } else {
            FineClaimMessages.sendError(player, result.message());
        }
    }

    @FunctionalInterface
    private interface PlayerAction {
        void run(Player player);
    }
}
