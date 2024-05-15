package at.hannibal2.skyhanni.config.features.garden;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.features.garden.CropType;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.List;

public class NextJacobContestConfig {
    @Expose
    @ConfigOption(name = "Show Jacob's Contest", desc = "Show the current or next Jacob's farming contest time and crops.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean display = true;

    @Expose
    @ConfigOption(name = "Outside Garden", desc = "Show the timer not only in the Garden but everywhere in SkyBlock.")
    @ConfigEditorBoolean
    public boolean showOutsideGarden = false;

    @Expose
    @ConfigOption(name = "In Other Guis", desc = "Mark the current or next Farming Contest crops in other farming GUIs as underlined.")
    @ConfigEditorBoolean
    public boolean otherGuis = false;

    @Expose
    @ConfigOption(name = "Fetch Contests", desc = "Automatically fetch Contests from elitebot.dev for the current year if they're uploaded already.")
    @ConfigEditorBoolean
    public boolean fetchAutomatically = true;

    @Expose
    @ConfigOption(name = "Share Contests", desc = "Share the list of upcoming Contests to elitebot.dev for everyone else to then fetch automatically.")
    @ConfigEditorDropdown
    public ShareContestsEntry shareAutomatically = ShareContestsEntry.ASK;

    public enum ShareContestsEntry {
        ASK("Ask When Needed"),
        AUTO("Share Automatically"),
        DISABLED("Disabled"),
        ;

        private final String str;

        ShareContestsEntry(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    @Expose
    @ConfigOption(name = "Warning", desc = "Show a warning shortly before a new Jacob's Contest starts.")
    @ConfigEditorBoolean
    public boolean warn = false;

    @Expose
    @ConfigOption(name = "Warning Time", desc = "Set the warning time in seconds before a Jacob's Contest begins.")
    @ConfigEditorSlider(
        minValue = 10,
        maxValue = 60 * 5,
        minStep = 1
    )
    public int warnTime = 60 * 2;

    @Expose
    @ConfigOption(name = "Popup Warning", desc = "Opens a popup when the warning time is reached and Minecraft is not in focus.")
    @ConfigEditorBoolean
    public boolean warnPopup = false;

    @Expose
    @ConfigOption(
        name = "Warn For",
        desc = "Only warn for these crops."
    )
    @ConfigEditorDraggableList
    public List<CropType> warnFor = new ArrayList<>(CropType.getEntries());

    @Expose
    @ConfigLink(owner = NextJacobContestConfig.class, field = "display")
    public Position pos = new Position(-200, 10, false, true);
}
