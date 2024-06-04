package at.hannibal2.skyhanni.config.features.crimsonisle;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;
import org.lwjgl.input.Keyboard;

public class ReputationHelperConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable features around Reputation features in the Crimson Isle.")
    @ConfigEditorBoolean
    @FeatureToggle
    public Property<Boolean> enabled = Property.of(false);

    @Expose
    @ConfigOption(name = "Hide Completed", desc = "Hides tasks after they've been completed.")
    @ConfigEditorBoolean
    public Property<Boolean> hideComplete = Property.of(true);

    @Expose
    @ConfigOption(name = "Use Hotkey", desc = "Only show the Reputation Helper while pressing the hotkey.")
    @ConfigEditorBoolean
    public boolean useHotkey = false;

    @Expose
    @ConfigOption(name = "Hotkey", desc = "Press this hotkey to show the Reputation Helper.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int hotkey = Keyboard.KEY_NONE;

    @Expose
    @ConfigLink(owner = ReputationHelperConfig.class, field = "enabled")
    public Position position = new Position(10, 10, false, true);

    @Expose
    @ConfigOption(name = "Show Locations", desc = "Crimson Isles waypoints for locations to get reputation.")
    @ConfigEditorDropdown
    public ShowLocationEntry showLocation = ShowLocationEntry.ONLY_HOTKEY;

    public enum ShowLocationEntry {
        ALWAYS("Always"),
        ONLY_HOTKEY("Only With Hotkey"),
        NEVER("Never");
        private final String str;

        ShowLocationEntry(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }
}
