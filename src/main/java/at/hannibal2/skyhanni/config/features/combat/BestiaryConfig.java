package at.hannibal2.skyhanni.config.features.combat;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class BestiaryConfig {
    @Expose
    @ConfigOption(name = "Enable", desc = "Show Bestiary Data overlay in the Bestiary menu.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Number format", desc = "Short: 1.1k\nLong: 1.100")
    @ConfigEditorDropdown
    public NumberFormatEntry numberFormat = NumberFormatEntry.SHORT;

    public enum NumberFormatEntry {
        SHORT("Short"),
        LONG("Long");

        private final String str;

        NumberFormatEntry(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    @Expose
    @ConfigOption(name = "Display type", desc = "Choose what the display should show")
    @ConfigEditorDropdown
    public DisplayTypeEntry displayType = DisplayTypeEntry.GLOBAL_MAX;

    public enum DisplayTypeEntry {
        GLOBAL_MAX("Global to max"),
        GLOBAL_NEXT("Global to next tier"),
        LOWEST_TOTAL("Lowest total kills"),
        HIGHEST_TOTAL("Highest total kills"),
        LOWEST_MAX("Lowest kills needed to max"),
        HIGHEST_MAX("Highest kills needed to max"),
        LOWEST_NEXT("Lowest kills needed to next tier"),
        HIGHEST_NEXT("Highest kills needed to next tier");

        private final String str;

        DisplayTypeEntry(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    @Expose
    @ConfigOption(name = "Hide maxed", desc = "Hide maxed mobs.")
    @ConfigEditorBoolean
    public boolean hideMaxed = false;

    @Expose
    @ConfigOption(name = "Replace Romans", desc = "Replace Roman numerals (IX) with regular numbers (9)")
    @ConfigEditorBoolean
    public boolean replaceRoman = false;

    @Expose
    @ConfigLink(owner = BestiaryConfig.class, field = "enabled")
    public Position position = new Position(100, 100, false, true);
}
