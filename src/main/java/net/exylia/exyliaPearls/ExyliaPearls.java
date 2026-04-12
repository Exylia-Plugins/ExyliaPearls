package net.exylia.exyliaPearls;

import net.exylia.commons.v2.loader.ExyliaLoaderPlugin;
import net.exylia.commons.v2.debug.api.DebugAPI;
import net.exylia.commons.v2.tasks.api.TaskAPI;
import net.exylia.exyliaPearls.listener.AntiGlitchListener;
import net.exylia.exyliaPearls.service.AntiGlitchService;

import static org.bukkit.Bukkit.getServer;

public final class ExyliaPearls extends ExyliaLoaderPlugin {

    @Override
    protected void onPreExyliaEnable() {
    }

    @Override
    protected void onExyliaEnable() {
        DebugAPI.init(getPlugin());
        TaskAPI.initialize(getPlugin());

        AntiGlitchService antiGlitchService = new AntiGlitchService();
        getServer().getPluginManager().registerEvents(new AntiGlitchListener(antiGlitchService), getPlugin());

        DebugAPI.logPluginSuccess("ExyliaPearls enabled.");
    }

    @Override
    protected void onExyliaDisable() {
        TaskAPI.shutdown();
    }
}
