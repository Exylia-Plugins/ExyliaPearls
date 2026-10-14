package net.exylia.exyliaPearls.config;

import net.exylia.lib.config.Comment;
import net.exylia.lib.config.Key;
import net.exylia.lib.config.Languages;
import net.exylia.lib.config.Migration;
import net.exylia.exyliaPearls.model.CheckMode;

/**
 * Plugin settings, stored in {@code config.yml}.
 *
 * <p>Read once at load. The values are consulted on every pearl tick, so going
 * back to the file for them would parse the same two numbers thousands of times
 * a minute.
 */
public record PearlSettings(
        @Comment("Language of this plugin's menus and messages. 'default' follows the language set in")
        @Comment("ExyliaLib's config.yml; en, es, pt, fr or ru set this plugin alone. Each one is a folder under")
        @Comment("lang/ you can edit; 'custom' holds the files this server had before languages existed.")
        String language,
        @Key("ender-pearls")
        @Comment("Settings related to ender pearl behaviour.")
        EnderPearls enderPearls) {

    public PearlSettings() {
        this(Languages.DEFAULT, new EnderPearls());
    }

    /** Version 1 to 2: a server that ran the plugin before languages keeps its files, in {@code lang/custom/}. */
    public static final Migration MIGRATION_FROM_1 = Languages.ADOPT_EXISTING;

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
