package net.exylia.exyliaPearls.util;

import net.exylia.commons.v2.debug.api.DebugAPI;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.*;

import javax.annotation.Nullable;

public final class GlitchUtil {

    private static final int MAX_ITERATIONS = 10;

    private GlitchUtil() {}

    @Nullable
    public static Location fix(Location original) {
        Location loc = original.clone();
        DebugAPI.logPluginDebug("[Fix] Starting fix for " + formatLoc(loc));
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            DebugAPI.logPluginDebug("[Fix] Iteration " + (i + 1) + ": Y=" + String.format("%.2f", loc.getY()));
            FixResult r = step(loc);
            DebugAPI.logPluginDebug("[Fix] Step result: " + r + " -> " + formatLoc(loc));
            if (r == FixResult.OK) {
                DebugAPI.logPluginDebug("[Fix] OK! Final location: " + formatLoc(loc));
                return loc;
            }
            if (r == FixResult.CANCEL) {
                DebugAPI.logPluginDebug("[Fix] CANCELLED! Attempting to lower Y...");
                double currentY = loc.getY();
                loc.setY(currentY - 0.5);
                if (loc.getY() < original.getY() - 2.0) {
                    DebugAPI.logPluginDebug("[Fix] Lowered too much, giving up!");
                    return null;
                }
                continue;
            }
        }
        DebugAPI.logPluginDebug("[Fix] MAX ITERATIONS exceeded!");
        return null;
    }

    private static FixResult step(Location loc) {
        Block feet = loc.getBlock();
        double yFrac = loc.getY() - feet.getY();
        BlockData data = feet.getBlockData();

        DebugAPI.logPluginDebug("[Step] Block: " + feet.getType() + " (passable=" + feet.isPassable() + ")");
        DebugAPI.logPluginDebug("[Step] yFrac=" + String.format("%.2f", yFrac) + " (Y=" + String.format("%.2f", loc.getY()) + ")");

        if (data instanceof Slab slab) {
            DebugAPI.logPluginDebug("[Step] SLAB detected: " + slab.getType());
            return switch (slab.getType()) {
                case BOTTOM -> {
                    if (yFrac < 0.5) {
                        DebugAPI.logPluginDebug("[Step] SLAB BOTTOM: yFrac < 0.5, moving to +0.5");
                        loc.setY(feet.getY() + 0.5);
                        yield FixResult.MODIFIED;
                    }
                    yield clearanceCheck(loc);
                }
                case TOP -> {
                    if (yFrac >= 0.5) {
                        DebugAPI.logPluginDebug("[Step] SLAB TOP: yFrac >= 0.5, pushing up");
                        yield pushUp(loc, feet);
                    }
                    yield clearanceCheck(loc);
                }
                case DOUBLE -> {
                    DebugAPI.logPluginDebug("[Step] SLAB DOUBLE: pushing up");
                    yield pushUp(loc, feet);
                }
            };
        }

        if (data instanceof Fence || data instanceof Wall) {
            DebugAPI.logPluginDebug("[Step] FENCE/WALL detected, moving to +1.5");
            loc.setY(feet.getY() + 1.5);
            return FixResult.MODIFIED;
        }

        if (data instanceof Gate gate) {
            DebugAPI.logPluginDebug("[Step] GATE detected, open=" + gate.isOpen());
            if (!gate.isOpen()) {
                DebugAPI.logPluginDebug("[Step] GATE closed, moving to +1.5");
                loc.setY(feet.getY() + 1.5);
                return FixResult.MODIFIED;
            }
            return clearanceCheck(loc);
        }

        if (data instanceof TrapDoor trap && !trap.isOpen()) {
            double surface = trap.getHalf() == Bisected.Half.BOTTOM ? 0.1875 : 1.0;
            DebugAPI.logPluginDebug("[Step] TRAPDOOR detected, surface=" + String.format("%.4f", surface));
            if (yFrac < surface) {
                DebugAPI.logPluginDebug("[Step] TRAPDOOR: yFrac < surface, moving to surface");
                loc.setY(feet.getY() + surface);
                return FixResult.MODIFIED;
            }
            return clearanceCheck(loc);
        }

        if (data instanceof Stairs stairs) {
            double stairHeight = getStairHeight(stairs, yFrac);
            DebugAPI.logPluginDebug("[Step] STAIRS detected, height=" + String.format("%.2f", stairHeight));
            if (yFrac < stairHeight) {
                DebugAPI.logPluginDebug("[Step] STAIRS: yFrac < height, moving to height");
                loc.setY(feet.getY() + stairHeight);
                return FixResult.MODIFIED;
            }
            if (yFrac > stairHeight) {
                DebugAPI.logPluginDebug("[Step] STAIRS: yFrac > height, pushing up");
                return pushUp(loc, feet);
            }
            return clearanceCheck(loc);
        }

        if (!feet.isPassable()) {
            double blockTop = feet.getY() + 1.0;
            if (loc.getY() < blockTop) {
                DebugAPI.logPluginDebug("[Step] Block NOT passable, moving to block top (Y=" + String.format("%.2f", blockTop) + ")");
                loc.setY(blockTop);
                return FixResult.MODIFIED;
            }
            DebugAPI.logPluginDebug("[Step] Block NOT passable but already above it, lowering Y");
            loc.setY(loc.getY() - 0.5);
            return FixResult.MODIFIED;
        }

        Block below = feet.getRelative(BlockFace.DOWN);
        BlockData belowData = below.getBlockData();
        if (belowData instanceof Fence || belowData instanceof Wall
                || (belowData instanceof Gate g && !g.isOpen())) {
            double fenceTop = below.getY() + 1.5;
            DebugAPI.logPluginDebug("[Step] Block below is FENCE/WALL/CLOSED_GATE at " + String.format("%.2f", fenceTop));
            if (loc.getY() < fenceTop) {
                DebugAPI.logPluginDebug("[Step] Adjusting Y to fence top");
                loc.setY(fenceTop);
                return FixResult.MODIFIED;
            }
        }

        DebugAPI.logPluginDebug("[Step] Passable block, checking clearance");
        return clearanceCheck(loc);
    }

    private static double getStairHeight(Stairs stairs, double yFrac) {
        Half half = stairs.getHalf();
        if (half == Half.BOTTOM) return 0.5;
        return 1.0;
    }

    private static FixResult pushUp(Location loc, Block feet) {
        Block above1 = feet.getRelative(BlockFace.UP);
        Block above2 = above1.getRelative(BlockFace.UP);

        boolean above1Pass = above1.isPassable();
        boolean above2Pass = above2.isPassable();

        DebugAPI.logPluginDebug("[PushUp] Y+1: " + above1.getType() + " (passable=" + above1Pass + ")");
        DebugAPI.logPluginDebug("[PushUp] Y+2: " + above2.getType() + " (passable=" + above2Pass + ")");

        if (!above1Pass || !above2Pass) {
            DebugAPI.logPluginDebug("[PushUp] BLOCKED! Cannot push up");
            return FixResult.CANCEL;
        }

        DebugAPI.logPluginDebug("[PushUp] Pushing to Y+1.0");
        loc.setY(feet.getY() + 1.0);
        return FixResult.MODIFIED;
    }

    private static FixResult clearanceCheck(Location loc) {
        Block feet = loc.getBlock();
        Block head = feet.getRelative(BlockFace.UP);

        boolean feetPass = feet.isPassable();
        boolean headPass = head.isPassable();

        DebugAPI.logPluginDebug("[ClearanceCheck] Y: " + feet.getType() + " (passable=" + feetPass + ")");
        DebugAPI.logPluginDebug("[ClearanceCheck] Y+1: " + head.getType() + " (passable=" + headPass + ")");

        if (!feetPass) {
            DebugAPI.logPluginDebug("[ClearanceCheck] BLOCKED! Feet not passable");
            return FixResult.CANCEL;
        }

        if (!headPass) {
            DebugAPI.logPluginDebug("[ClearanceCheck] Head blocked but feet ok, allowing (tight space)");
            return FixResult.OK;
        }

        DebugAPI.logPluginDebug("[ClearanceCheck] ALL CLEAR!");
        return FixResult.OK;
    }

    private static String formatLoc(Location loc) {
        if (loc == null) return "null";
        return String.format("X:%.2f Y:%.2f Z:%.2f", loc.getX(), loc.getY(), loc.getZ());
    }

    private enum FixResult { OK, MODIFIED, CANCEL }
}
