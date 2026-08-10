package net.exylia.exyliaPearls.command;

import net.exylia.exyliaPearls.ExyliaPearls;
import net.exylia.exyliaPearls.service.DebugService;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

public class CommandRegistry {

    private final Lamp<BukkitCommandActor> lamp;

    public CommandRegistry(ExyliaPearls plugin, DebugService debugService) {
        this.lamp = BukkitLamp.builder(plugin.getPlugin()).build();
        registerCommands(debugService);
    }

    private void registerCommands(DebugService debugService) {
        lamp.register(new PearlsCommand(debugService));
    }
}
