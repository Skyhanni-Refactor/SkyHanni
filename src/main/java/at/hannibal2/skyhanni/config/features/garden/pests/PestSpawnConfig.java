package at.hannibal2.skyhanni.config.features.garden.pests;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class PestSpawnConfig {

    @Expose
    @ConfigOption(
        name = "Chat Message Format",
        desc = "Change how the pest spawn chat message should be formatted.")
    @ConfigEditorDropdown
    public ChatMessageFormatEntry chatMessageFormat = ChatMessageFormatEntry.HYPIXEL;

    public enum ChatMessageFormatEntry {
        HYPIXEL("Hypixel Style"),
        COMPACT("Compact"),
        DISABLED("Disabled");
        private final String str;

        ChatMessageFormatEntry(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    @Expose
    @ConfigOption(
        name = "Show Title",
        desc = "Show a Title when a pest spawns."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showTitle = true;
}
