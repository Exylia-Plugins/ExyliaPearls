package net.exylia.exyliaPearls.config;

import net.exylia.lib.config.Comment;
import net.exylia.lib.config.Key;
import net.exylia.exyliaPearls.model.CheckMode;

/**
 * Plugin settings, stored in {@code config.yml}.
 *
 * <p>Read once at load. The values are consulted on every pearl tick, so going
 * back to the file for them would parse the same two numbers thousands of times
 * a minute.
 */
public record PearlSettings(
        @Key("ender-pearls")
        @Comment("Settings related to ender pearl behaviour.")
        EnderPearls enderPearls) {

    public PearlSettings() {
        this(new EnderPearls());
    }

    public record EnderPearls(
            @Comment("Height-ratio multiplier used when checking bounding boxes.")
            @Comment("A taller box is stricter, so fewer landing spots count as safe.")
            @Comment("FULL    - Checks the entire body of the player.")
            @Comment("PARTIAL - Checks the sneak height of the player.")
            @Comment("HALF    - Checks one full block.")
            @Comment("NORMAL  - Recommended.")
            @Comment("Unknown values fall back to NORMAL.")
            String mode,

            @Key("check-tick-interval")
            @Comment("Interval, in ticks, at which bounding box checks run while a pearl is in flight.")
            @Comment("Raising this lowers tracking accuracy, so only change it if you hit performance issues.")
            @Comment("Recommended: 1 | Minimum: 1")
            int checkTickInterval) {

        public EnderPearls() {
            this(CheckMode.NORMAL.name(), 1);
        }
    }


    /** The configured mode, resolved from its string form. */
    public CheckMode mode() {
        return CheckMode.fromString(enderPearls.mode());
    }

    /** The configured tick interval, never below one tick. */
    public int checkTickInterval() {
        return Math.max(enderPearls.checkTickInterval(), 1);
    }
}
