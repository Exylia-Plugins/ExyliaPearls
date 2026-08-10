package net.exylia.exyliaPearls.listener;

import net.exylia.exyliaPearls.service.DebugService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Clears debug state when a player leaves, so it does not survive a rejoin.
 */
public final class PlayerQuitListener implements Listener {

    private final DebugService debugService;

    public PlayerQuitListener(DebugService debugService) {
        this.debugService = debugService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        debugService.stopWatching(event.getPlayer().getUniqueId());
    }
}
