package net.exylia.exyliaPearls.command;

import net.exylia.commons.v2.reload.api.ReloadAPI;
import net.exylia.commons.v2.visual.api.MessageAPI;
import net.exylia.exyliaPearls.config.MessagesDefaults;
import net.exylia.exyliaPearls.service.DebugService;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"exyliapearls", "pearls"})
@CommandPermission("exyliapearls.admin")
public class PearlsCommand {

    private final DebugService debugService;

    public PearlsCommand(DebugService debugService) {
        this.debugService = debugService;
    }

    @Subcommand("debug")
    public void debug(Player player) {
        boolean enabled = debugService.toggle(player.getUniqueId());
        MessageAPI.send(player, enabled
            ? MessagesDefaults.Debug.ENABLED
            : MessagesDefaults.Debug.DISABLED);
    }

    @Subcommand("reload")
    public void reload(Player player) {
        ReloadAPI.getInstance().reloadAll(player);
    }
}
