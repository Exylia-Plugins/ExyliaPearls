package net.exylia.exyliaPearls.util;

import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Collision helpers used to determine whether a location can safely hold a player.
 *
 * <p>Port of the approach used by PearlFix (https://github.com/OutDev0/PearlFix).</p>
 */
public final class BoundingBoxUtils {

    private BoundingBoxUtils() {
    }

    /**
     * Checks whether the given {@link Location} is safe to be teleported to.
     *
     * @param location  location used to perform the check.
     * @param halfWidth the width of the bounding box divided by two.
     * @param height    the height of the bounding box.
     * @param viewers   players watching the debug visualisation; empty to skip rendering.
     * @return true if the checked location is safe.
     */
    public static boolean isSafe(@NotNull Location location, double halfWidth, double height, List<Player> viewers) {
        World world = location.getWorld();
        if (world == null) return false;

        final BoundingBox box = getBoundingBoxAt(location, halfWidth, height);
        final boolean safe = !hasAnyBlockCollisions(box, world, viewers);

        if (!viewers.isEmpty()) {
            drawBoundingBox(box, world, safe ? Color.LIME : Color.RED, viewers);
        }
        return safe;
    }

    /**
     * Checks whether the given {@link BoundingBox} overlaps the bounding box of any neighbouring block.
     *
     * @param boundingBox the box being checked for block collisions.
     * @param world       the world the blocks are retrieved from.
     * @param viewers     players watching the debug visualisation; empty to skip rendering.
     * @return true if the box overlaps with any block.
     */
    public static boolean hasAnyBlockCollisions(@NotNull BoundingBox boundingBox, @NotNull World world, List<Player> viewers) {
        int minX = (int) Math.floor(boundingBox.getMinX());
        int maxX = (int) Math.floor(boundingBox.getMaxX());
        int minY = (int) Math.floor(boundingBox.getMinY());
        int maxY = (int) Math.floor(boundingBox.getMaxY());
        int minZ = (int) Math.floor(boundingBox.getMinZ());
        int maxZ = (int) Math.floor(boundingBox.getMaxZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (!block.isSolid()) continue;

                    final BoundingBox box = block.getBoundingBox();
                    final boolean overlaps = box.overlaps(boundingBox);

                    if (!viewers.isEmpty()) {
                        drawBoundingBox(box, world, overlaps ? Color.YELLOW : Color.WHITE, viewers);
                    }

                    if (overlaps) return true;
                }
            }
        }

        return false;
    }

    /**
     * Creates a {@link BoundingBox} standing on the given {@link Location}.
     *
     * @param location  where the box should be created.
     * @param halfWidth the width of the box divided by two.
     * @param height    the height of the box.
     * @return a new box at the target location.
     */
    public static BoundingBox getBoundingBoxAt(@NotNull Location location, double halfWidth, double height) {
        return new BoundingBox(
            location.getX() - halfWidth,
            location.getY(),
            location.getZ() - halfWidth,
            location.getX() + halfWidth,
            location.getY() + height,
            location.getZ() + halfWidth
        );
    }

    /**
     * Visualises the edges of a {@link BoundingBox} using {@link Particle#DUST},
     * sent only to the given viewers.
     *
     * @param boundingBox the box that should be drawn.
     * @param world       the world in which the box should be drawn.
     * @param color       the colour of the particle.
     * @param viewers     players that should receive the particles.
     */
    public static void drawBoundingBox(BoundingBox boundingBox, World world, Color color, List<Player> viewers) {
        if (viewers.isEmpty()) return;

        double minX = boundingBox.getMinX();
        double minY = boundingBox.getMinY();
        double minZ = boundingBox.getMinZ();

        double maxX = boundingBox.getMaxX();
        double maxY = boundingBox.getMaxY();
        double maxZ = boundingBox.getMaxZ();

        final double step = 0.2;

        ParticleBuilder particle = new ParticleBuilder(Particle.DUST)
            .color(color)
            .count(1)
            .extra(0)
            .receivers(viewers);

        for (double x = minX; x <= maxX; x += step) {
            particle.location(world, x, minY, minZ).spawn();
            particle.location(world, x, minY, maxZ).spawn();
            particle.location(world, x, maxY, minZ).spawn();
            particle.location(world, x, maxY, maxZ).spawn();
        }

        for (double y = minY; y <= maxY; y += step) {
            particle.location(world, minX, y, minZ).spawn();
            particle.location(world, maxX, y, minZ).spawn();
            particle.location(world, minX, y, maxZ).spawn();
            particle.location(world, maxX, y, maxZ).spawn();
        }

        for (double z = minZ; z <= maxZ; z += step) {
            particle.location(world, minX, minY, z).spawn();
            particle.location(world, maxX, minY, z).spawn();
            particle.location(world, minX, maxY, z).spawn();
            particle.location(world, maxX, maxY, z).spawn();
        }
    }
}
