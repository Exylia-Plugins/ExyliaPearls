package net.exylia.exyliaPearls.service;

import net.exylia.exyliaPearls.config.ConfigDefaults;
import net.exylia.exyliaPearls.model.CheckMode;
import net.exylia.exyliaPearls.model.PearlData;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Holds the tracked state of every in-flight ender pearl.
 *
 * <p>Entries expire automatically so a pearl that never fires a hit event (unloaded chunk,
 * server hiccup, shooter disconnect) cannot leak.</p>
 */
public final class PearlTrackerService {

    /** Minimum time between full sweeps of the map, so a per-tick put stays O(1). */
    private static final long PURGE_INTERVAL_MILLIS = 5_000L;

    private final Map<UUID, Entry> tracked = new ConcurrentHashMap<>();
    private final AtomicLong nextPurgeAt = new AtomicLong();

    public void put(UUID pearlId, PearlData data) {
        purgeExpiredIfDue();
        tracked.put(pearlId, new Entry(data, Instant.now()));
    }

    public PearlData get(UUID pearlId) {
        Entry entry = tracked.get(pearlId);
        if (entry == null) return null;
        if (isExpired(entry)) {
            tracked.remove(pearlId);
            return null;
        }
        return entry.data();
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
     * The player's height scaled by the configured {@link CheckMode}.
     */
    public static double heightOf(Player player) {
        return player.getHeight() * CheckMode.fromString(ConfigDefaults.EnderPearls.MODE).getMultiplier();
    }

    private void purgeExpiredIfDue() {
        long now = System.currentTimeMillis();
        long due = nextPurgeAt.get();
        if (now < due) return;
        if (!nextPurgeAt.compareAndSet(due, now + PURGE_INTERVAL_MILLIS)) return;
        tracked.entrySet().removeIf(entry -> isExpired(entry.getValue()));
    }

    private static boolean isExpired(Entry entry) {
        int seconds = Math.max(ConfigDefaults.EnderPearls.EXPIRE_AFTER_SECONDS, 1);
        return Duration.between(entry.createdAt(), Instant.now()).getSeconds() >= seconds;
    }

    private record Entry(PearlData data, Instant createdAt) {
    }
}
