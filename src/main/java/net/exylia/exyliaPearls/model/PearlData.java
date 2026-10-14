package net.exylia.exyliaPearls.model;

import org.bukkit.Location;

/**
 * Tracked state of an in-flight ender pearl.
 *
 * @param origin   the location the pearl was thrown from, used as the ultimate fallback.
 * @param lastSafe the most recent location the pearl occupied that can safely hold the player.
 */
public record PearlData(Location origin, Location lastSafe) {
}
