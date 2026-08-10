package net.exylia.exyliaPearls.listener;

import net.exylia.exyliaPearls.config.ConfigDefaults;
import net.exylia.exyliaPearls.model.PearlData;
import net.exylia.exyliaPearls.service.PearlTrackerService;
import net.exylia.exyliaPearls.util.BoundingBoxUtils;
import net.exylia.exyliaPearls.util.DebugHook;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Redirects the player to the last recorded safe location when a pearl lands somewhere
 * the player would clip into a block. Safe landings are left entirely to vanilla.
 */
public final class ProjectileHitListener implements Listener {

    /** Vanilla damage applied after teleporting with an ender pearl. */
    private static final double PEARL_DAMAGE = 5.0;

    private final PearlTrackerService tracker;

    public ProjectileHitListener(PearlTrackerService tracker) {
        this.tracker = tracker;
    }

    @SuppressWarnings("UnstableApiUsage") // DamageSource
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!ConfigDefaults.EnderPearls.ENABLED) return;
        if (!(event.getEntity() instanceof EnderPearl pearl)) return;
        if (!(pearl.getShooter() instanceof Player player)) return;

        final PearlData data = tracker.get(pearl.getUniqueId());
        if (data == null) return; // should not happen

        // pre-calculated once to avoid recomputing per check
        final double halfWidth = PearlTrackerService.halfWidthOf(player);
        final double height = PearlTrackerService.heightOf(player);
        final DebugHook debug = DebugHook.getDebugHook(ConfigDefaults.DEBUG);

        // the landing spot is fine, let vanilla handle it
        if (BoundingBoxUtils.isSafe(pearl.getLocation(), halfWidth, height, debug)) {
            return;
        }

        final Location destination = data.lastSafe().clone();

        // remove the pearl so vanilla does not teleport the player into a block
        pearl.remove();
        tracker.invalidate(pearl.getUniqueId());

        // the recorded location is no longer safe, do nothing
        if (!BoundingBoxUtils.isSafe(destination, halfWidth, height, debug)) {
            return;
        }

        destination.setYaw(player.getYaw());
        destination.setPitch(player.getPitch());

        // preserves the original pearl teleport metadata
        player.teleportAsync(destination, PlayerTeleportEvent.TeleportCause.ENDER_PEARL).thenAccept(teleported -> {
            if (!teleported) return;

            // apply the vanilla pearl effects after the teleport;
            // built as a proper damage source so fall damage is calculated correctly
            DamageSource damageSource = DamageSource.builder(DamageType.FALL)
                .withCausingEntity(player)
                .withDirectEntity(pearl)
                .withDamageLocation(destination)
                .build();

            player.damage(PEARL_DAMAGE, damageSource);
            destination.getWorld().playSound(destination, Sound.ENTITY_PLAYER_TELEPORT, 1F, 1F);
        });
    }
}
