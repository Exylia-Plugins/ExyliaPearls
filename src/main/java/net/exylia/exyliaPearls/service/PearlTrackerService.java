package net.exylia.exyliaPearls.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.exylia.exyliaPearls.ExyliaPearls;
import net.exylia.exyliaPearls.model.PearlData;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Holds the tracked state of every in-flight ender pearl.
 *
 * <p>An entry is dropped when the pearl lands, and expires on its own otherwise.
 * A plain map would have relied on the flight task always getting to run its
 * cleanup, which a chunk unloading in the wrong moment does not guarantee — and
 * a pearl thrown into an unloaded region would then be remembered forever.
 */
public final class PearlTrackerService {

    /**
     * How long a pearl stays tracked without news.
     *
     * <p>A vanilla pearl lives a minute at most, so anything still here after
     * two is a pearl the server lost track of, not one in flight.
     */
    private static final long MAX_FLIGHT_MINUTES = 2;

    /** {@link #MAX_FLIGHT_MINUTES} in ticks, after which a pearl's flight task stops tracking it. */
    public static final int MAX_FLIGHT_TICKS = (int) (MAX_FLIGHT_MINUTES * 60 * 20);

    private final ExyliaPearls plugin;
    private final Cache<UUID, PearlData> tracked = Caffeine.newBuilder()
            .expireAfterWrite(MAX_FLIGHT_MINUTES, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    public PearlTrackerService(ExyliaPearls plugin) {
        this.plugin = plugin;
    }

    public void put(UUID pearlId, PearlData data) {
        tracked.put(pearlId, data);
    }

    public PearlData get(UUID pearlId) {
        return tracked.getIfPresent(pearlId);
    }

    public void invalidate(UUID pearlId) {
        tracked.invalidate(pearlId);
    }

    public void invalidateAll() {
        tracked.invalidateAll();
    }

    /** Half the width of the player's bounding box. */
    public static double halfWidthOf(Player player) {
        return player.getWidth() / 2;
    }

    /** The player's height, scaled by the configured {@link net.exylia.exyliaPearls.model.CheckMode}. */
    public double heightOf(Player player) {
        return player.getHeight() * plugin.settings().mode().getMultiplier();
    }
}
