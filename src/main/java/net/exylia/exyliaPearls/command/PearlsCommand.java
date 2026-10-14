package net.exylia.exyliaPearls.command;

import net.exylia.lib.text.Text;
import net.exylia.exyliaPearls.ExyliaPearls;
import net.exylia.exyliaPearls.service.DebugService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"exyliapearls", "pearls"})
@CommandPermission("exyliapearls.admin")
public class PearlsCommand {

    private final ExyliaPearls plugin;
    private final DebugService debugService;

    public PearlsCommand(ExyliaPearls plugin, DebugService debugService) {
        this.plugin = plugin;
        this.debugService = debugService;
    }

    @Subcommand("debug")
    public void debug(Player player) {
        boolean enabled = debugService.toggle(player.getUniqueId());
        var debug = plugin.messages().debug();

        // from(), not of(): the prefix belongs to this plugin, and only a
        // message that says where it came from can resolve %prefix%.
        Text.from(plugin.getPlugin(), enabled ? debug.enabled() : debug.disabled()).send(player);
    }

    /**
     * Reloads this plugin only.
     *
     * <p>Not the library: recolouring every plugin on the server is what
     * {@code /exylialib reload} is for.
     */
    @Subcommand("reload")
    public void reload(CommandSender sender) {
        plugin.reload(sender);
    }
}
