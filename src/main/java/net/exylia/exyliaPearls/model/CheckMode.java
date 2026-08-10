package net.exylia.exyliaPearls.model;

import java.util.Locale;

/**
 * Height-ratio multiplier applied to the player bounding box during collision checks.
 *
 * <p>A taller box is stricter: fewer locations qualify as safe, so the player is more
 * likely to be pulled back to an earlier point instead of landing where the pearl did.</p>
 */
public enum CheckMode {
    /** Checks the entire body of the player. */
    FULL(1.0),
    /** Checks the sneak height of the player. */
    PARTIAL(1.5 / 1.8),
    /** Checks one full block. */
    HALF(1.0 / 1.8),
    /** Swim height. Recommended, and the default. */
    NORMAL(0.6 / 1.8);

    private final double multiplier;

    CheckMode(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }

    /**
     * Resolves a mode by name, falling back to {@link #NORMAL} when the value is unknown.
     *
     * <p>The config stores this as a plain string because the schema loader assigns raw
     * YAML values straight into the field without converting them to enum constants.</p>
     */
    public static CheckMode fromString(String name) {
        if (name == null) return NORMAL;
        try {
            return valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return NORMAL;
        }
    }
}
