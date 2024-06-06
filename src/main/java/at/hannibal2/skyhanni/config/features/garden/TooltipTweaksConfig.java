package at.hannibal2.skyhanni.config.features.garden;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class TooltipTweaksConfig {
    @Expose
    @ConfigOption(
        name = "Compact Descriptions",
        desc = "Hides redundant parts of reforge descriptions, generic counter description, and Farmhand perk explanation."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean compactToolTooltips = false;

    @Expose
    @ConfigOption(
        name = "Breakdown Hotkey",
        desc = "When the keybind is pressed, show a breakdown of all fortune sources on a tool."
    )
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_LSHIFT)
    public int fortuneTooltipKeybind = Keyboard.KEY_LSHIFT;

    @Expose
    @ConfigOption(
        name = "Tooltip Format",
        desc = "Show crop-specific Farming Fortune in tooltip.\n" +
            "§fShow: §7Crop-specific Fortune indicated as §6[+196]\n" +
            "§fReplace: §7Edits the total Fortune to include crop-specific Fortune."
    )
    @ConfigEditorDropdown
    public CropTooltipFortuneEntry cropTooltipFortune = CropTooltipFortuneEntry.SHOW;

    public enum CropTooltipFortuneEntry {
        DEFAULT("Default"),
        SHOW("Show"),
        REPLACE("Replace");
        private final String str;

        CropTooltipFortuneEntry(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    @Expose
    @ConfigOption(
        name = "Total Crop Milestone",
        desc = "Shows the progress bar till maxed crop milestone in the crop milestone inventory."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean cropMilestoneTotalProgress = true;
}
