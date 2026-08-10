package net.exylia.exyliaPearls;

import net.exylia.commons.v2.config.schema.ConfigSchemaRegistry;
import net.exylia.commons.v2.debug.api.DebugAPI;
import net.exylia.commons.v2.loader.ExyliaLoaderPlugin;
import net.exylia.exyliaPearls.config.ConfigDefaults;
import net.exylia.exyliaPearls.listener.ProjectileHitListener;
import net.exylia.exyliaPearls.listener.ProjectileLaunchListener;
import net.exylia.exyliaPearls.service.PearlTrackerService;

public final class ExyliaPearls extends ExyliaLoaderPlugin {

    private PearlTrackerService trackerService;

    @Override
    protected void onExyliaEnable() {
        DebugAPI.init(getPlugin());
        ConfigSchemaRegistry.ensureDefaults(ConfigDefaults.class);

        trackerService = new PearlTrackerService();
        registerListeners();

        DebugAPI.sendPluginMOTD(getPlugin());
    }

    @Override
    protected void onExyliaDisable() {
        if (trackerService != null) trackerService.invalidateAll();
    }

    private void registerListeners() {
        getPlugin().getServer().getPluginManager()
            .registerEvents(new ProjectileLaunchListener(trackerService), getPlugin());
        getPlugin().getServer().getPluginManager()
            .registerEvents(new ProjectileHitListener(trackerService), getPlugin());
    }
}
