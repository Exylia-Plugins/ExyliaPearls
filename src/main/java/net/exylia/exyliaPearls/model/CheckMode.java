package net.exylia.exyliaPearls.model;

import net.exylia.exyliaPearls.util.BoundingBoxUtils;

/**
 * Height-ratio multiplier applied to the player bounding box during collision checks.
 */
public enum CheckMode {
    /** Checks the entire body of the player. */
    FULL(1.0),
    /** Checks the sneak height of the player. */
    PARTIAL(BoundingBoxUtils.SNEAK_HEIGHT_RATIO),
    /** Checks one full block. */
    HALF(BoundingBoxUtils.BLOCK_HEIGHT_RATIO),
    /** Recommended size. */
    NORMAL(BoundingBoxUtils.SWIM_HEIGHT_RATIO);

    private final double multiplier;

    CheckMode(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }

    /**
     * Resolves a mode by name, falling back to {@link #NORMAL} when the value is unknown.
     */
    public static CheckMode fromString(String name) {
        if (name == null) return NORMAL;
        try {
            return valueOf(name.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return NORMAL;
        }
    }
}
