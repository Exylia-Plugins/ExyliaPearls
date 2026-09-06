package net.exylia.exyliaPearls;

import com.lukittu.loader.LoaderPlugin;
import com.lukittu.loader.spigot.SpigotLukittuLoader;
import net.exylia.lib.config.ConfigFile;
import net.exylia.lib.config.Configs;
import net.exylia.lib.debug.Debug;
import net.exylia.lib.reload.Reloads;
import net.exylia.lib.text.Prefixes;
import net.exylia.exyliaPearls.api.PearlsServiceImpl;
import net.exylia.exyliaPearls.command.CommandRegistry;
import net.exylia.exyliaPearls.config.PearlSettings;
import net.exylia.exyliaPearls.listener.PlayerQuitListener;
import net.exylia.exyliaPearls.listener.ProjectileHitListener;
import net.exylia.exyliaPearls.listener.ProjectileLaunchListener;
import net.exylia.exyliaPearls.service.DebugService;
import net.exylia.exyliaPearls.service.PearlTrackerService;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Stops an ender pearl from putting a player inside a block.
 *
 * <p>A pearl in flight remembers the last place its thrower would have fitted.
 * If it lands somewhere they would clip into terrain, they go to that remembered
 * place instead. A safe landing is left entirely to vanilla.
 */
public final class ExyliaPearls implements LoaderPlugin<SpigotLukittuLoader> {

    private JavaPlugin plugin;
    private Debug debug;
    private ConfigFile<PearlSettings> settings;
    private PearlTrackerService trackerService;
    private DebugService debugService;
    private CommandRegistry commands;
    private Reloads reloads;

    @Override
    public void start(SpigotLukittuLoader loader) {
        plugin = loader.getPlugin();
        debug = Debug.of(plugin);

        settings = Configs.define(plugin, "config", PearlSettings.class).load();
        Prefixes.set(plugin, settings.get().messages().prefix());

        debugService = new DebugService();
        trackerService = new PearlTrackerService(this);
        commands = new CommandRegistry(this, debugService);
        registerListeners();

        // After the tracker exists and before anything can ask for the service:
        // a plugin that enables later finds it, and one that enabled earlier is
        // told by Bukkit when it appears.
        PearlsServiceImpl.register(loader, this, trackerService);

        reloads = Reloads.of(plugin).step("configs", () -> {
            Configs.reloadAll(plugin);
            // The prefix was read once; a reload can have changed it.
            Prefixes.set(plugin, settings.get().messages().prefix());
        });

        debug.motd();
    }

    @Override
    public void shutdown(SpigotLukittuLoader loader) {
        if (trackerService != null) trackerService.invalidateAll();
        if (debugService != null) debugService.clear();
        if (commands != null) commands.unregister();
    }

    private void registerListeners() {
        PluginManager plugins = plugin.getServer().getPluginManager();
        plugins.registerEvents(new ProjectileLaunchListener(this, trackerService, debugService), plugin);
        plugins.registerEvents(new ProjectileHitListener(trackerService, debugService), plugin);
        plugins.registerEvents(new PlayerQuitListener(debugService), plugin);
    }

    public JavaPlugin getPlugin() { return plugin; }
    public Debug debug() { return debug; }
    public PearlSettings settings() { return settings.get(); }
    public void reload(CommandSender sender) { reloads.run(sender); }
}
