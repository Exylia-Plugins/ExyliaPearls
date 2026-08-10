package net.exylia.exyliaPearls;

import net.exylia.commons.v2.debug.api.DebugAPI;
import net.exylia.commons.v2.loader.ExyliaLoaderPlugin;
import net.exylia.exyliaPearls.command.CommandRegistry;
import net.exylia.exyliaPearls.config.MessagesDefaults;
import net.exylia.exyliaPearls.listener.PlayerQuitListener;
import net.exylia.exyliaPearls.listener.ProjectileHitListener;
import net.exylia.exyliaPearls.listener.ProjectileLaunchListener;
import net.exylia.exyliaPearls.service.DebugService;
import net.exylia.exyliaPearls.service.PearlTrackerService;
import org.bukkit.plugin.PluginManager;

public final class ExyliaPearls extends ExyliaLoaderPlugin {

    private PearlTrackerService trackerService;
    private DebugService debugService;

    @Override
    protected void onPreExyliaEnable() {
        debugService = new DebugService();
        new CommandRegistry(this, debugService);
    }

    @Override
    protected void onExyliaEnable() {
        DebugAPI.init(getPlugin());
        MessagesDefaults.register();

        trackerService = new PearlTrackerService();
        registerListeners();

        DebugAPI.sendPluginMOTD(getPlugin());
    }

    @Override
    protected void onExyliaDisable() {
        if (trackerService != null) trackerService.invalidateAll();
        if (debugService != null) debugService.clear();
    }

    private void registerListeners() {
        PluginManager pluginManager = getPlugin().getServer().getPluginManager();
        pluginManager.registerEvents(new ProjectileLaunchListener(trackerService, debugService), getPlugin());
        pluginManager.registerEvents(new ProjectileHitListener(trackerService, debugService), getPlugin());
        pluginManager.registerEvents(new PlayerQuitListener(debugService), getPlugin());
    }
}
