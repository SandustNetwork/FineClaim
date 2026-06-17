package com.sandustnetwork.fineclaim;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

final class PaperCommandRegistration {

    private PaperCommandRegistration() {
    }

    static void register(JavaPlugin plugin, String name, String description, BasicCommand command) {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(name, description, command);
        });
    }
}
