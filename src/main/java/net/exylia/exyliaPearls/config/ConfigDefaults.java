package net.exylia.exyliaPearls.config;

import net.exylia.commons.v2.config.schema.Comment;
import net.exylia.commons.v2.config.schema.ConfigSchema;
import net.exylia.commons.v2.config.schema.ConfigSchemaRegistry;
import net.exylia.commons.v2.config.schema.ConfigSection;
import net.exylia.commons.v2.config.schema.ConfigValue;
import net.exylia.exyliaPearls.model.CheckMode;

/**
 * Plugin settings.
 *
 * <p>Everything lives under {@code ender-pearls}. The top-level {@code debug} key belongs to
 * ExyliaCommons and must not be declared here.</p>
 */
@ConfigSchema(file = "config", version = "1.0")
public class ConfigDefaults {

    public static void register() {
        ConfigSchemaRegistry.ensureDefaults(ConfigDefaults.class);
    }

    @ConfigSection("ender-pearls")
    @Comment("Settings related to ender pearl behaviour.")
    public static class EnderPearls {

        @ConfigValue("mode")
        @Comment({
            "Height-ratio multiplier used when checking bounding boxes.",
            "A taller box is stricter, so fewer landing spots count as safe.",
            "FULL    - Checks the entire body of the player.",
            "PARTIAL - Checks the sneak height of the player.",
            "HALF    - Checks one full block.",
            "NORMAL  - Recommended.",
            "Unknown values fall back to NORMAL."
        })
        public static String MODE = CheckMode.NORMAL.name();

        @ConfigValue("check-tick-interval")
        @Comment({
            "Interval, in ticks, at which bounding box checks run while a pearl is in flight.",
            "Raising this lowers tracking accuracy, so only change it if you hit performance issues.",
            "Recommended: 1 | Minimum: 1"
        })
        public static int CHECK_TICK_INTERVAL = 1;
    }

    /**
     * The configured mode, resolved from its string form.
     */
    public static CheckMode mode() {
        return CheckMode.fromString(EnderPearls.MODE);
    }

    /**
     * The configured tick interval, clamped to at least one tick.
     */
    public static int checkTickInterval() {
        return Math.max(EnderPearls.CHECK_TICK_INTERVAL, 1);
    }
}
