package com.sandustnetwork.fineclaim.claim.util;

import com.sandustnetwork.fineclaim.claim.domain.ClaimBox;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

public final class FineClaimMessages {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final DateTimeFormatter CREATED_AT_FORMAT = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());
    private static final int STRIKETHROUGH_SEPARATOR_LENGTH = 28;
    private static final String STRIKETHROUGH_SEPARATOR = " ".repeat(STRIKETHROUGH_SEPARATOR_LENGTH);

    private static final String ICON_SUCCESS =
            "<dark_gray>[</dark_gray><green><bold>✔</bold></green><dark_gray>]</dark_gray>";
    private static final String ICON_ERROR =
            "<dark_gray>[</dark_gray><red><bold>✘</bold></red><dark_gray>]</dark_gray>";
    private static final String ICON_WARNING =
            "<dark_gray>[</dark_gray><yellow><bold>!</bold></yellow><dark_gray>]</dark_gray>";
    private static final String ICON_INFO =
            "<dark_gray>[</dark_gray><aqua><bold>i</bold></aqua><dark_gray>]</dark_gray>";
    private static final String ICON_CANCEL =
            "<dark_gray>[</dark_gray><gray><bold>↩</bold></gray><dark_gray>]</dark_gray>";

    private FineClaimMessages() {
    }

    public static void sendSuccess(CommandSender sender, String message) {
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(message, "message");
        sender.sendMessage(MINI_MESSAGE.deserialize(
                ICON_SUCCESS + " <white><message>",
                Placeholder.unparsed("message", message)
        ));
    }

    public static void sendError(CommandSender sender, String message) {
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(message, "message");
        sender.sendMessage(MINI_MESSAGE.deserialize(
                ICON_ERROR + " <white><message>",
                Placeholder.unparsed("message", message)
        ));
    }

    public static void sendWarning(CommandSender sender, String message) {
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(message, "message");
        sender.sendMessage(MINI_MESSAGE.deserialize(
                ICON_WARNING + " <yellow><message>",
                Placeholder.unparsed("message", message)
        ));
    }

    public static void sendInfo(CommandSender sender, String message) {
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(message, "message");
        sender.sendMessage(MINI_MESSAGE.deserialize(
                ICON_INFO + " <gray><message>",
                Placeholder.unparsed("message", message)
        ));
    }

    public static void sendCancelled(CommandSender sender, String message) {
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(message, "message");
        sender.sendMessage(MINI_MESSAGE.deserialize(
                ICON_CANCEL + " <gray><message>",
                Placeholder.unparsed("message", message)
        ));
    }

    public static void sendClaimInfoPanel(
            Player player,
            String ownerName,
            ClaimBox box,
            int trustedCount,
            Instant createdAt
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(ownerName, "ownerName");
        Objects.requireNonNull(box, "box");
        Objects.requireNonNull(createdAt, "createdAt");

        sendStrikethroughSeparator(player);
        player.sendMessage(MINI_MESSAGE.deserialize(
                ICON_INFO + " <gold><bold>Claim Information</bold></gold>"
        ));
        sendStrikethroughSeparator(player);
        sendClaimInfoRow(player, "Owner", ownerName);
        sendClaimInfoRow(player, "World", box.worldName());
        sendClaimInfoRow(player, "Size", box.sizeX() + " x " + box.sizeY() + " x " + box.sizeZ());
        sendClaimInfoRow(player, "Blocks", String.valueOf(box.volume()));
        sendClaimInfoRow(player, "From", formatCorner(box.minX(), box.minY(), box.minZ()));
        sendClaimInfoRow(player, "To", formatCorner(box.maxX(), box.maxY(), box.maxZ()));
        sendClaimInfoRow(player, "Trusted", formatTrustedCount(trustedCount));
        sendClaimInfoRow(player, "Created", formatCreatedAt(createdAt));
        sendStrikethroughSeparator(player);
    }

    private static String formatCorner(int x, int y, int z) {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    private static void sendStrikethroughSeparator(Player player) {
        player.sendMessage(MINI_MESSAGE.deserialize(
                "<dark_gray><st><line></st></dark_gray>",
                Placeholder.unparsed("line", STRIKETHROUGH_SEPARATOR)
        ));
    }

    private static void sendClaimInfoRow(Player player, String label, String value) {
        player.sendMessage(MINI_MESSAGE.deserialize(
                " <dark_gray>▸</dark_gray> <aqua><label></aqua> <dark_gray>»</dark_gray> <white><value>",
                Placeholder.unparsed("label", label),
                Placeholder.unparsed("value", value)
        ));
    }

    public static String formatCreatedAt(Instant createdAt) {
        Objects.requireNonNull(createdAt, "createdAt");
        return CREATED_AT_FORMAT.format(createdAt);
    }

    public static String resolvePlayerName(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        String name = offlinePlayer.getName();
        if (name == null || name.isBlank()) {
            return "Unknown";
        }
        return name;
    }

    private static String formatTrustedCount(int trustedCount) {
        if (trustedCount == 1) {
            return "1 player";
        }
        return trustedCount + " players";
    }
}
