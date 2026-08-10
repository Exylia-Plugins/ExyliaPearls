package net.exylia.exyliaPearls.listener;

import net.exylia.commons.v2.tasks.api.TaskAPI;
import net.exylia.exyliaPearls.config.ConfigDefaults;
import net.exylia.exyliaPearls.model.PearlData;
import net.exylia.exyliaPearls.service.PearlTrackerService;
import net.exylia.exyliaPearls.util.BoundingBoxUtils;
import net.exylia.exyliaPearls.util.DebugHook;
import org.bukkit.Location;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.util.UUID;

/**
 * Records the last location an in-flight pearl occupied that could safely hold the player.
 */
public final class ProjectileLaunchListener implements Listener {

    private final PearlTrackerService tracker;

    public ProjectileLaunchListener(PearlTrackerService tracker) {
        this.tracker = tracker;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!ConfigDefaults.EnderPearls.ENABLED) return;
        if (!(event.getEntity() instanceof EnderPearl pearl)) return;
        if (!(pearl.getShooter() instanceof Player player)) return;

        final UUID pearlId = pearl.getUniqueId();

        // the throw location acts as the fallback
        final Location origin = player.getLocation().clone();
        tracker.put(pearlId, new PearlData(origin, origin));

        // pre-calculated once to avoid recomputing every tick
        final double halfWidth = PearlTrackerService.halfWidthOf(player);
        final double height = PearlTrackerService.heightOf(player);

        final int interval = Math.max(ConfigDefaults.EnderPearls.CHECK_TICK_INTERVAL, 1);

        // scheduled on the pearl, so it dies together with the entity
        TaskAPI.atTimer(pearl, () -> {
            final Location current = pearl.getLocation();
            final DebugHook debug = DebugHook.getDebugHook(ConfigDefaults.DEBUG);

            // only record the location when the player would fit there
            if (BoundingBoxUtils.isSafe(current, halfWidth, height, debug)) {
                tracker.put(pearlId, new PearlData(origin, current.clone()));
            }
        }, () -> tracker.invalidate(pearlId), interval, interval);
    }
}
