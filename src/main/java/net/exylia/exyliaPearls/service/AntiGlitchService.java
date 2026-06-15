package net.exylia.exyliaPearls.service;

import net.exylia.commons.v2.debug.api.DebugAPI;
import net.exylia.commons.v2.tasks.api.TaskAPI;
import net.exylia.commons.v2.tasks.scheduler.ScheduledTask;
import net.exylia.exyliaPearls.util.GlitchUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public final class AntiGlitchService {

    private static final double PEARL_DAMAGE = 5.0;

    private final ConcurrentHashMap<UUID, PearlTracker> trackers = new ConcurrentHashMap<>();

    public void startTracking(Player player, Projectile pearl) {
        UUID uuid = player.getUniqueId();
        PearlTracker existing = trackers.remove(uuid);
        if (existing != null && existing.task != null) {
            existing.task.cancel();
        }

        PearlTracker tracker = new PearlTracker(pearl, pearl.getLocation().clone());
        trackers.put(uuid, tracker);
        tracker.task = TaskAPI.atTimer(pearl, () -> tick(uuid), 0L, 1L);
    }

    public void handlePearlLand(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (!(projectile.getShooter() instanceof Player player)) return;

        UUID uuid = player.getUniqueId();
        PearlTracker tracker = trackers.remove(uuid);
        if (tracker != null && tracker.task != null) {
            tracker.task.cancel();
        }

        Location lastSafe = tracker != null ? tracker.lastSafe.get() : null;

        org.bukkit.entity.Entity hitEntity = event.getHitEntity();
        if (hitEntity instanceof LivingEntity target && target != player) {
            double speed = projectile.getVelocity().length();
            target.damage(speed, player);
        }

        event.setCancelled(true);
        projectile.remove();

        Location pearlLoc = projectile.getLocation();
        Block hitBlock = event.getHitBlock();
        BlockFace hitFace = event.getHitBlockFace();

        DebugAPI.logPluginDebug("[Pearl] Player: " + player.getName());
        DebugAPI.logPluginDebug("[Pearl] Pearl landed at: " + formatLoc(pearlLoc));
        DebugAPI.logPluginDebug("[Pearl] Hit block: " + hitBlock + ", Face: " + hitFace);
        DebugAPI.logPluginDebug("[Pearl] LastSafe: " + (lastSafe != null ? formatLoc(lastSafe) : "null"));

        if (hitBlock == null) {
            DebugAPI.logPluginDebug("[Pearl] No hit block (entity/expiry) - teleporting to pearl location");
            final Location entityDest = pearlLoc.clone();
            TaskAPI.at(player, () -> {
                Location playerLoc = player.getLocation();
                entityDest.setYaw(playerLoc.getYaw());
                entityDest.setPitch(playerLoc.getPitch());
                teleportWithVelocity(player, entityDest);
                player.damage(PEARL_DAMAGE);
            });
            return;
        }

        if (hitFace == BlockFace.DOWN) {
            DebugAPI.logPluginDebug("[Pearl] DOWN face detected - using lastSafe only");
            final Location finalDest;
            if (lastSafe != null) {
                finalDest = lastSafe.clone();
            } else {
                finalDest = null;
            }
            TaskAPI.at(player, () -> {
                if (finalDest != null) {
                    Location playerLoc = player.getLocation();
                    finalDest.setYaw(playerLoc.getYaw());
                    finalDest.setPitch(playerLoc.getPitch());
                    DebugAPI.logPluginDebug("[Pearl] Teleporting to lastSafe: " + formatLoc(finalDest));
                    teleportWithVelocity(player, finalDest);
                } else {
                    DebugAPI.logPluginDebug("[Pearl] NO VALID DEST - NOT TELEPORTING!");
                }
                player.damage(PEARL_DAMAGE);
            });
            return;
        }

        Location dest = computeDest(pearlLoc, hitBlock, hitFace);
        DebugAPI.logPluginDebug("[Pearl] Computed dest: " + formatLoc(dest));

        Location fixed = GlitchUtil.fix(dest);
        DebugAPI.logPluginDebug("[Pearl] Fixed dest: " + (fixed != null ? formatLoc(fixed) : "null"));

        if (fixed == null && lastSafe != null) {
            DebugAPI.logPluginDebug("[Pearl] Fixed dest was null, trying lastSafe...");
            fixed = GlitchUtil.fix(lastSafe.clone());
            DebugAPI.logPluginDebug("[Pearl] Fixed lastSafe: " + (fixed != null ? formatLoc(fixed) : "null"));
        }

        final Location finalDest = fixed;
        TaskAPI.at(player, () -> {
            if (finalDest != null) {
                Location playerLoc = player.getLocation();
                finalDest.setYaw(playerLoc.getYaw());
                finalDest.setPitch(playerLoc.getPitch());
                DebugAPI.logPluginDebug("[Pearl] Teleporting to: " + formatLoc(finalDest));
                teleportWithVelocity(player, finalDest);
            } else {
                DebugAPI.logPluginDebug("[Pearl] NO VALID DEST - NOT TELEPORTING!");
            }
            player.damage(PEARL_DAMAGE);
        });
    }

    private static String formatLoc(Location loc) {
        if (loc == null) return "null";
        return String.format("X:%.2f Y:%.2f Z:%.2f (Block: %d,%d,%d)",
                loc.getX(), loc.getY(), loc.getZ(),
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public void cleanup(UUID playerUUID) {
        PearlTracker tracker = trackers.remove(playerUUID);
        if (tracker != null && tracker.task != null) {
            tracker.task.cancel();
        }
    }

    private void tick(UUID playerUUID) {
        PearlTracker tracker = trackers.get(playerUUID);
        if (tracker == null) return;
        if (!tracker.pearl.isValid()) {
            cleanup(playerUUID);
            return;
        }
        Location loc = tracker.pearl.getLocation();
        Block block = loc.getBlock();
        if (block.isPassable() && block.getRelative(BlockFace.UP).isPassable()) {
            tracker.lastSafe.set(loc.clone());
        }
    }

    private static Location computeDest(Location pearlLoc, Block hitBlock, BlockFace hitFace) {
        Location dest = pearlLoc.clone();
        if (hitBlock == null || hitFace == null) return dest;

        double bx = hitBlock.getX();
        double bz = hitBlock.getZ();
        double by = hitBlock.getY();

        switch (hitFace) {
            case EAST  -> { dest.setX(bx + 1.6); dest.setZ(bz + 0.5); }
            case WEST  -> { dest.setX(bx - 0.6); dest.setZ(bz + 0.5); }
            case SOUTH -> { dest.setZ(bz + 1.6); dest.setX(bx + 0.5); }
            case NORTH -> { dest.setZ(bz - 0.6); dest.setX(bx + 0.5); }
            case UP    -> { dest.setY(by + 1.0); dest.setX(bx + 0.5); dest.setZ(bz + 0.5); }
            case DOWN  -> { dest.setY(by - 0.1); dest.setX(bx + 0.5); dest.setZ(bz + 0.5); }
        }

        return dest;
    }

    private static void teleportWithVelocity(Player player, Location dest) {
        Vector velocity = player.getVelocity();
        player.teleportAsync(dest, PlayerTeleportEvent.TeleportCause.ENDER_PEARL)
                .thenAccept(success -> {
                    if (success) TaskAPI.at(player, () -> player.setVelocity(velocity));
                });
    }

    private static final class PearlTracker {
        final Projectile pearl;
        final AtomicReference<Location> lastSafe = new AtomicReference<>();
        volatile ScheduledTask task;

        PearlTracker(Projectile pearl, Location initialSafe) {
            this.pearl = pearl;
            this.lastSafe.set(initialSafe);
        }
    }
}
