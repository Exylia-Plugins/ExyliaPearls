package net.exylia.exyliaPearls.listener;

import net.exylia.lib.task.Tasks;
import net.exylia.exyliaPearls.ExyliaPearls;
import net.exylia.exyliaPearls.model.PearlData;
import net.exylia.exyliaPearls.service.DebugService;
import net.exylia.exyliaPearls.service.PearlTrackerService;
import net.exylia.exyliaPearls.util.BoundingBoxUtils;
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

    private final ExyliaPearls plugin;
    private final PearlTrackerService tracker;
    private final DebugService debugService;

    public ProjectileLaunchListener(ExyliaPearls plugin, PearlTrackerService tracker, DebugService debugService) {
        this.plugin = plugin;
        this.tracker = tracker;
        this.debugService = debugService;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof EnderPearl pearl)) return;
        if (!(pearl.getShooter() instanceof Player player)) return;

        final UUID pearlId = pearl.getUniqueId();

        // the throw location acts as the fallback
        final Location origin = player.getLocation().clone();
        tracker.put(pearlId, new PearlData(origin, origin));

        // pre-calculated once to avoid recomputing every tick
        final double halfWidth = PearlTrackerService.halfWidthOf(player);
        final double height = tracker.heightOf(player);

        final int interval = plugin.settings().checkTickInterval();

        // scheduled on the pearl, so it stops when the entity is gone; a pearl held in
        // stasis (or whose owner left) is never going to land, so stop watching it too
        Tasks.of(plugin.getPlugin()).runAtEntityTimer(pearl, interval, interval, handle -> {
            if (pearl.getTicksLived() > PearlTrackerService.MAX_FLIGHT_TICKS || !player.isOnline()) {
                handle.cancel();
                tracker.invalidate(pearlId);
                return;
            }

            final Location current = pearl.getLocation();

            // only record the location when the player would fit there
            if (BoundingBoxUtils.isSafe(current, halfWidth, height, debugService.viewers())) {
                tracker.put(pearlId, new PearlData(origin, current.clone()));
            }
        });
    }
}
