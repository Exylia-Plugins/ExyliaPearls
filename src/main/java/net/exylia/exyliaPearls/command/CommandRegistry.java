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
        lamp.register(new PearlsCommand(plugin, debugService));
    }

    /**
     * Drops the commands when the plugin stops.
     *
     * <p>Without this a command survives a reload pointing at a dead plugin,
     * and the next player to run it gets an exception instead of an answer.
     */
    public void unregister() {
        lamp.unregisterAllCommands();
    }
}
