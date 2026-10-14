package net.exylia.exyliaPearls.config;

import net.exylia.lib.config.Comment;

/**
 * What the plugin says, stored in {@code messages.yml}.
 *
 * <p>Colours are written as palette tokens rather than hex, so the server owner
 * recolours everything from {@code colors.yml} instead of editing this file.
 */
public record PearlMessages(
        @Comment("Prefixed onto this plugin's messages")
        String prefix,

        Debug debug) {

    public PearlMessages() {
        this("{primary}&lEXYLIA PEARLS &8•&r", new Debug());
    }

    /** Sent when a staff member toggles collision box rendering. */
    public record Debug(
            @Comment("Sent when a staff member turns collision box rendering on")
            String enabled,

            @Comment("Sent when they turn it off")
            String disabled) {

        public Debug() {
            this("[sound:BLOCK_NOTE_BLOCK_PLING|1.0|1.0]%prefix% {success}Debug {highlight}enabled{success}. "
                            + "{muted}Collision boxes are now visible.",
                    "%prefix% {muted}Debug {highlight}disabled{muted}.");
        }
    }
}
