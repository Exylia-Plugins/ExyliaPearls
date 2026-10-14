package net.exylia.exyliaPearls.api;

import net.exylia.exyliaPearls.ExyliaPearls;
import net.exylia.exyliaPearls.model.PearlData;
import net.exylia.exyliaPearls.service.PearlTrackerService;
import net.exylia.exyliaPearls.util.BoundingBoxUtils;
import net.exylia.lib.api.pearls.CheckMode;
import net.exylia.lib.api.pearls.PearlsService;
import net.exylia.lib.api.pearls.TrackedPearl;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * What third-party plugins see of ExyliaPearls.
 *
 * <p>A translation layer over {@link PearlTrackerService} and the plugin's
 * settings, and nothing more. The only work done here is turning the plugin's
 * own types into the records the API publishes — those types live in this
 * plugin's classloader, which no other plugin can reach.
 *
 * <p>The plugin's own listeners keep calling the tracker directly. Routing
 * internal calls through a service lookup would mean asking Bukkit for a
 * reference to ourselves on every pearl tick.
 *
 * @see net.exylia.lib.api.pearls.PearlsService
 */
public final class PearlsServiceImpl implements PearlsService {

    private final ExyliaPearls plugin;
    private final PearlTrackerService tracker;

    private PearlsServiceImpl(ExyliaPearls plugin, PearlTrackerService tracker) {
        this.plugin = plugin;
        this.tracker = tracker;
    }

    /**
     * Publishes the service so other plugins can find it.
     *
     * <p>Registered against the {@code JavaPlugin}, which is what
     * Bukkit knows about: unregistration then happens by itself when the plugin
     * disables, so a reload cannot leave a stale implementation behind.
     *
     * <p>The tracker is handed in rather than looked up, because it is a field
     * of the plugin with no accessor and publishing the API is not a reason to
     * grow one.
     *
     * @param bukkit  the Bukkit plugin hosting this one
     * @param plugin  the plugin, for its settings
     * @param tracker the tracker holding every in-flight pearl
     */
    public static void register(@NotNull JavaPlugin bukkit, @NotNull ExyliaPearls plugin,
                                @NotNull PearlTrackerService tracker) {
        bukkit.getServer().getServicesManager()
                .register(PearlsService.class, new PearlsServiceImpl(plugin, tracker), bukkit,
                        ServicePriority.Normal);
    }

    @Override
    public boolean isTracked(@NotNull UUID pearlId) {
        return tracker.get(pearlId) != null;
    }

    @Override
    @NotNull
    public Optional<TrackedPearl> pearl(@NotNull UUID pearlId) {
        PearlData data = tracker.get(pearlId);
        if (data == null) return Optional.empty();
        return Optional.of(new TrackedPearl(data.origin().clone(), data.lastSafe().clone()));
    }

    @Override
    @NotNull
    public Optional<Location> fallbackDestination(@NotNull UUID pearlId) {
        PearlData data = tracker.get(pearlId);
        return data == null ? Optional.empty() : Optional.of(data.lastSafe().clone());
    }

    @Override
    public boolean fitsAt(@NotNull Player player, @NotNull Location location) {
        return BoundingBoxUtils.isSafe(location, PearlTrackerService.halfWidthOf(player),
                tracker.heightOf(player), List.of());
    }

    @Override
    @NotNull
    public CheckMode checkMode() {
        return switch (plugin.settings().mode()) {
            case FULL -> CheckMode.FULL;
            case PARTIAL -> CheckMode.PARTIAL;
            case HALF -> CheckMode.HALF;
            case NORMAL -> CheckMode.NORMAL;
        };
    }

    @Override
    public int checkTickInterval() {
        return plugin.settings().checkTickInterval();
    }
}
