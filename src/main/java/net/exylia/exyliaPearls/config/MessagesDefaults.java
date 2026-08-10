package net.exylia.exyliaPearls.config;

import net.exylia.commons.v2.config.schema.ConfigSchema;
import net.exylia.commons.v2.config.schema.ConfigSchemaRegistry;
import net.exylia.commons.v2.config.schema.ConfigSection;
import net.exylia.commons.v2.config.schema.ConfigValue;

@ConfigSchema(file = "messages", strict = true)
public class MessagesDefaults {

    public static void register() {
        ConfigSchemaRegistry.ensureDefaults(MessagesDefaults.class);
    }

    @ConfigValue("prefix")
    public static String PREFIX = "{primary}&lEXYLIA PEARLS &8•&r";

    @ConfigSection("debug")
    public static class Debug {
        @ConfigValue("enabled")
        public static String ENABLED = "[sound:BLOCK_NOTE_BLOCK_PLING|1.0|1.0]%prefix% {success}Debug {highlight}enabled{success}. {muted}Collision boxes are now visible.";

        @ConfigValue("disabled")
        public static String DISABLED = "%prefix% {muted}Debug {highlight}disabled{muted}.";
    }
}
