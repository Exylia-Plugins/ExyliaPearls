package net.exylia.exyliaPearls.service;

import net.exylia.exyliaPearls.config.ConfigDefaults;
import net.exylia.exyliaPearls.model.PearlData;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds the tracked state of every in-flight ender pearl.
 *
 * <p>Entries are removed when the pearl lands or when its scheduled task stops, which
 * covers the pearl expiring, despawning or its chunk unloading.</p>
 */
public final class PearlTrackerService {

    private final Map<UUID, PearlData> tracked = new ConcurrentHashMap<>();

    public void put(UUID pearlId, PearlData data) {
        tracked.put(pearlId, data);
    }

    public PearlData get(UUID pearlId) {
        return tracked.get(pearlId);
    }

    public void invalidate(UUID pearlId) {
        tracked.remove(pearlId);
    }

    public void invalidateAll() {
        tracked.clear();
    }

    /**
     * Half the width of the player's bounding box.
     */
    public static double halfWidthOf(Player player) {
        return player.getWidth() / 2;
    }

    /**
     * The player's height scaled by the configured {@link net.exylia.exyliaPearls.model.CheckMode}.
     */
    public static double heightOf(Player player) {
        return player.getHeight() * ConfigDefaults.mode().getMultiplier();
    }
}
