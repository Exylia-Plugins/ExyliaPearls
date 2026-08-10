package net.exylia.exyliaPearls.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

/**
 * Collision helpers used to determine whether a location can safely hold a player.
 *
 * <p>Port of the approach used by PearlFix (https://github.com/OutDev0/PearlFix).</p>
 */
public final class BoundingBoxUtils {

    /**
     * Height ratio applied to the player bounding box during collision checks.
     * Matches the swimming/crawling height, which is what PearlFix uses by default.
     */
    public static final double HEIGHT_RATIO = (0.6 / 1.8);

    private BoundingBoxUtils() {
    }

    /**
     * Checks whether the given {@link Location} is safe to be teleported to.
     *
     * @param location  location used to perform the check.
     * @param halfWidth the width of the bounding box divided by two.
     * @param height    the height of the bounding box.
     * @return true if the checked location is safe.
     */
    public static boolean isSafe(@NotNull Location location, double halfWidth, double height) {
        World world = location.getWorld();
        if (world == null) return false;

        return !hasAnyBlockCollisions(getBoundingBoxAt(location, halfWidth, height), world);
    }

    /**
     * Checks whether the given {@link BoundingBox} overlaps the bounding box of any neighbouring block.
     *
     * @param boundingBox the box being checked for block collisions.
     * @param world       the world the blocks are retrieved from.
     * @return true if the box overlaps with any block.
     */
    public static boolean hasAnyBlockCollisions(@NotNull BoundingBox boundingBox, @NotNull World world) {
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

                    if (block.getBoundingBox().overlaps(boundingBox)) return true;
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
}
