package at.hannibal2.skyhanni.config.features.itemability;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class FireVeilWandConfig {
    @Expose
    @ConfigOption(name = "Fire Veil Design", desc = "Changes the flame particles of the Fire Veil Wand ability.")
    @ConfigEditorDropdown
    public DisplayEntry display = DisplayEntry.PARTICLES;

    public enum DisplayEntry {
        PARTICLES("Particles"),
        LINE("Line"),
        OFF("Off"),
        ;
        private final String str;

        DisplayEntry(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    @Expose
    @ConfigOption(
        name = "Line Color",
        desc = "Changes the color of the Fire Veil Wand line."
    )
    @ConfigEditorColour
    public String displayColor = "0:245:255:85:85";
}
