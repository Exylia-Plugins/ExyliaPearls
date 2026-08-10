package net.exylia.exyliaPearls.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which staff members are currently visualising pearl collision boxes.
 *
 * <p>State is kept in memory only and toggled through the command, so debugging never
 * touches the configuration and never leaks between restarts.</p>
 */
public final class DebugService {

    private final Set<UUID> watching = ConcurrentHashMap.newKeySet();

    /**
     * Toggles debug for the given player.
     *
     * @return true if debug is now enabled for them.
     */
    public boolean toggle(UUID playerId) {
        if (!watching.add(playerId)) {
            watching.remove(playerId);
            return false;
        }
        return true;
    }

    public boolean isWatching(UUID playerId) {
        return watching.contains(playerId);
    }

    public void stopWatching(UUID playerId) {
        watching.remove(playerId);
    }

    public boolean isAnyoneWatching() {
        return !watching.isEmpty();
    }

    /**
     * The online players that should receive debug particles.
     *
     * @return an empty list when nobody is watching, so callers can skip rendering entirely.
     */
    public List<Player> viewers() {
        if (watching.isEmpty()) return List.of();

        List<Player> players = new ArrayList<>(watching.size());
        for (UUID id : watching) {
            Player player = Bukkit.getPlayer(id);
            if (player != null && player.isOnline()) players.add(player);
        }
        return players;
    }

    public void clear() {
        watching.clear();
    }
}
