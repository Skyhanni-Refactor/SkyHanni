package at.hannibal2.skyhanni.config.features.rift.motes;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class RiftInventoryValueConfig {
    @Expose
    @ConfigOption(name = "Inventory Value", desc = "Show total Motes NPC price for the current opened inventory.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Number Format Type", desc = "Short: 1.2M\n" +
        "Long: 1,200,000")
    @ConfigEditorDropdown
    public NumberFormatEntry formatType = NumberFormatEntry.SHORT;

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
    @ConfigLink(owner = RiftInventoryValueConfig.class, field = "enabled")
    public Position position = new Position(126, 156, false, true);
}
