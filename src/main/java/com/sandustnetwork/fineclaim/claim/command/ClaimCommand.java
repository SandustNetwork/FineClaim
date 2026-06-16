package com.sandustnetwork.fineclaim.claim.command;

import com.sandustnetwork.fineclaim.claim.application.ClaimOperationResult;
import com.sandustnetwork.fineclaim.claim.application.ClaimService;
import com.sandustnetwork.fineclaim.claim.domain.Claim;
import com.sandustnetwork.fineclaim.claim.domain.ClaimBox;
import com.sandustnetwork.fineclaim.claim.util.ClaimLocationMapper;
import com.sandustnetwork.fineclaim.claim.util.FineClaimMessages;
import com.sandustnetwork.fineclaim.claim.visual.ClaimPreviewManager;
import com.sandustnetwork.fineclaim.claim.wand.ClaimWandManager;
import com.sandustnetwork.fineclaim.permission.FineClaimPermission;
import com.sandustnetwork.fineclaim.permission.PermissionChecker;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class ClaimCommand implements BasicCommand {

    private static final List<String> ROOT_SUBCOMMANDS = List.of("confirm", "cancel", "resize");
    private static final List<String> RESIZE_SUBCOMMANDS = List.of("confirm", "cancel");

    private final ClaimService claimService;
    private final PermissionChecker permissionChecker;
    private final ClaimWandManager wandManager;
    private final ClaimPreviewManager previewManager;

    public ClaimCommand(
            ClaimService claimService,
            PermissionChecker permissionChecker,
            ClaimWandManager wandManager,
            ClaimPreviewManager previewManager
    ) {
        this.claimService = Objects.requireNonNull(claimService, "claimService");
        this.permissionChecker = Objects.requireNonNull(permissionChecker, "permissionChecker");
        this.wandManager = Objects.requireNonNull(wandManager, "wandManager");
        this.previewManager = Objects.requireNonNull(previewManager, "previewManager");
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();
        if (!(sender instanceof Player player)) {
            FineClaimMessages.sendError(sender, "This command can only be used by players.");
            return;
        }

        if (!permissionChecker.hasPermission(player, FineClaimPermission.COMMAND_CLAIM)) {
            FineClaimMessages.sendError(player, "You do not have permission to use this command.");
            return;
        }

        if (args.length == 0) {
            wandManager.startCreate(player);
            return;
        }

        if (args[0].equalsIgnoreCase("resize")) {
            handleResize(player, args);
            return;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "confirm" -> handleConfirm(player);
            case "cancel" -> handleCancel(player);
            default -> FineClaimMessages.sendWarning(
                    player,
                    "Usage: /claim, /claim confirm, /claim cancel, /claim resize [confirm|cancel]"
            );
        }
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (!(source.getSender() instanceof Player player)) {
            return List.of();
        }
        if (!permissionChecker.hasPermission(player, FineClaimPermission.COMMAND_CLAIM)) {
            return List.of();
        }

        if (args.length == 1) {
            return filterSuggestions(ROOT_SUBCOMMANDS, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("resize")) {
            return filterSuggestions(RESIZE_SUBCOMMANDS, args[1]);
        }
        return List.of();
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
            claimService.getClaimAtBlock(
                    selectedBox.get().worldName(),
                    selectedBox.get().minX(),
                    selectedBox.get().minY(),
                    selectedBox.get().minZ()
            ).ifPresent(claim -> previewManager.showClaimBorder(player, claim));
        }
        sendOperationResult(player, result);
    }

    private void handleCancel(Player player) {
        if (wandManager.getSession(player).isEmpty()) {
            FineClaimMessages.sendWarning(player, "You do not have an active claim selection.");
            return;
        }

        wandManager.endSession(player);
        FineClaimMessages.sendInfo(player, "Claim selection cancelled.");
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
            claimService.getClaimAtBlock(
                    selectedBox.get().worldName(),
                    selectedBox.get().minX(),
                    selectedBox.get().minY(),
                    selectedBox.get().minZ()
            ).ifPresent(claim -> previewManager.showClaimBorder(player, claim));
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
        FineClaimMessages.sendInfo(player, "Claim resize cancelled.");
    }

    static void sendOperationResult(Player player, ClaimOperationResult result) {
        if (result.success()) {
            FineClaimMessages.sendSuccess(player, result.message());
        } else {
            FineClaimMessages.sendError(player, result.message());
        }
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
