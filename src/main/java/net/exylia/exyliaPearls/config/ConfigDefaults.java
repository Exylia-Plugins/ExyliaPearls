package net.exylia.exyliaPearls.config;

import net.exylia.commons.v2.config.schema.Comment;
import net.exylia.commons.v2.config.schema.ConfigSchema;
import net.exylia.commons.v2.config.schema.ConfigSection;
import net.exylia.commons.v2.config.schema.ConfigValue;

@ConfigSchema(file = "config", version = "1.0")
public class ConfigDefaults {

    @ConfigValue("debug")
    @Comment("Draws the checked bounding boxes with particles. For advanced users only.")
    public static boolean DEBUG = false;

    @ConfigSection("ender-pearls")
    @Comment("Settings related to ender pearl behaviour.")
    public static class EnderPearls {

        @ConfigValue("enabled")
        @Comment("Enables or disables the ender pearl clipping patch.")
        public static boolean ENABLED = true;

        @ConfigValue("mode")
        @Comment({
            "Height-ratio multiplier used when checking bounding boxes.",
            "FULL    - Checks the entire body of the player.",
            "PARTIAL - Checks the sneak height of the player.",
            "HALF    - Checks one full block.",
            "NORMAL  - Recommended size."
        })
        public static String MODE = "NORMAL";

        @ConfigValue("check-tick-interval")
        @Comment({
            "Interval, in ticks, at which bounding box checks run.",
            "Changing this may drastically affect results.",
            "Recommended: 1 | Minimum: 1"
        })
        public static int CHECK_TICK_INTERVAL = 1;

        @ConfigValue("expire-after-seconds")
        @Comment({
            "Time after which tracked pearl data expires.",
            "A higher value helps counter race conditions.",
            "Recommended: 10 | Minimum: 1"
        })
        public static int EXPIRE_AFTER_SECONDS = 10;
    }
}
