package net.exylia.exyliaPearls.util;

import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

/**
 * Hook invoked after every collision check, used to visualise the checked bounding boxes.
 */
@FunctionalInterface
public interface DebugHook {

    void onCollisionCheck(@NotNull BoundingBox box, @NotNull World world, boolean collided);

    /** Does nothing; used when debug mode is disabled. */
    DebugHook NO_OP = (box, world, collided) -> {
    };

    /** Draws the checked bounding box: red when it collided, lime when it is clear. */
    DebugHook DRAW = (box, world, collided) ->
        BoundingBoxUtils.drawBoundingBox(box, world, collided ? Color.RED : Color.LIME);

    static DebugHook getDebugHook(boolean debug) {
        return debug ? DRAW : NO_OP;
    }
}
