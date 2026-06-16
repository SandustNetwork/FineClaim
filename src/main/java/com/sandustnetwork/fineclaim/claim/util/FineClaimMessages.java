package com.sandustnetwork.fineclaim.claim.util;

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
            .ofPattern("dd MMM yyyy, HH:mm")
            .withZone(ZoneId.systemDefault());

    private FineClaimMessages() {
    }

    public static void sendSuccess(CommandSender sender, String message) {
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(message, "message");
        sender.sendMessage(MINI_MESSAGE.deserialize(
                "<green><bold>✔</bold></green> <gray><message>",
                Placeholder.unparsed("message", message)
        ));
    }

    public static void sendError(CommandSender sender, String message) {
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(message, "message");
        sender.sendMessage(MINI_MESSAGE.deserialize(
                "<red><bold>✘</bold></red> <gray><message>",
                Placeholder.unparsed("message", message)
        ));
    }

    public static void sendWarning(CommandSender sender, String message) {
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(message, "message");
        sender.sendMessage(MINI_MESSAGE.deserialize(
                "<gold><bold>!</bold></gold> <yellow><message>",
                Placeholder.unparsed("message", message)
        ));
    }

    public static void sendInfo(CommandSender sender, String message) {
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(message, "message");
        sender.sendMessage(MINI_MESSAGE.deserialize(
                "<aqua><bold>FineClaim</bold></aqua> <dark_gray>»</dark_gray> <white><message>",
                Placeholder.unparsed("message", message)
        ));
    }

    public static void sendSectionHeader(Player player, String title) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(title, "title");
        player.sendMessage(MINI_MESSAGE.deserialize(
                "<gold><bold><title></bold></gold>",
                Placeholder.unparsed("title", title)
        ));
    }

    public static void sendLabeled(Player player, String label, String value) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(value, "value");
        player.sendMessage(MINI_MESSAGE.deserialize(
                "<gray><label>:</gray> <white><value>",
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

    public static String formatPlayerId(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        return playerId.toString();
    }
}
