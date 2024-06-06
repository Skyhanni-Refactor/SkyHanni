package at.hannibal2.skyhanni.config.features.garden.visitor;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.ACCEPTED;
import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.COINS_SPENT;
import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.COPPER;
import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.DEDICATION_IV;
import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.DENIED;
import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.FARMING_EXP;
import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.GREEN_BANDANA;
import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.OVERGROWN_GRASS;
import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.SPACER_1;
import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.TITLE;
import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.TOTAL_VISITORS;
import static at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry.VISITORS_BY_RARITY;

public class DropsStatisticsConfig {

    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "Tallies up statistic about visitors and the rewards you have received from them."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(
        name = "Text Format",
        desc = "Drag text to change the appearance of the overlay."
    )
    @ConfigEditorDraggableList
    public List<DropsStatisticsTextEntry> textFormat = new ArrayList<>(Arrays.asList(
        TITLE,
        TOTAL_VISITORS,
        VISITORS_BY_RARITY,
        ACCEPTED,
        DENIED,
        SPACER_1,
        COPPER,
        FARMING_EXP,
        COINS_SPENT,
        OVERGROWN_GRASS,
        GREEN_BANDANA,
        DEDICATION_IV
    ));

    /**
     * Do not change the order of the enums added to that list! New items are to be synced up with the implementation in GardenVisitorDropStatistics.drawDisplay.
     * Generic non VisitorReward stuff belongs in front of the first VisitorReward.
     */
    public enum DropsStatisticsTextEntry {
        // generic stuff
        TITLE("§e§lVisitor Statistics"),
        TOTAL_VISITORS("§e1,636 Total"),
        VISITORS_BY_RARITY("§a1,172§f-§9382§f-§681§f-§d2§f-§c1"),
        ACCEPTED("§21,382 Accepted"),
        DENIED("§c254 Denied"),
        SPACER_1(" "),
        COPPER("§c62,072 Copper"),
        FARMING_EXP("§33.2m Farming EXP"),
        COINS_SPENT("§647.2m Coins Spent"),
        SPACER_2(" "),
        GARDEN_EXP("§212,600 Garden EXP"),
        BITS("§b4.2k Bits"),
        MITHRIL_POWDER("§220k Mithril Powder"),
        GEMSTONE_POWDER("§d18k Gemstone Powder"),

        // VisitorReward items
        FLOWERING_BOUQUET("§b23 §9Flowering Bouquet"),
        OVERGROWN_GRASS("§b4 §9Overgrown Grass"),
        GREEN_BANDANA("§b2 §5Green Bandana"),
        DEDICATION_IV("§b1 §9Dedication IV"),
        MUSIC_RUNE_I("§b6 §b◆ Music Rune I"),
        SPACE_HELMET("§b1 §cSpace Helmet"),
        CULTIVATING_I("§b1 §9Cultivating I"),
        REPLENISH_I("§b1 §9Replenish I"),
        DELICATE("§b1 §9Delicate V"),
        ;

        private final String str;

        DropsStatisticsTextEntry(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    @Expose
    @ConfigOption(name = "Display Numbers First", desc = "Determines whether the number or drop name displays first. " +
        "§eNote: Will not update the preview above!")
    @ConfigEditorBoolean
    public boolean displayNumbersFirst = true;

    @Expose
    @ConfigOption(name = "Display Icons", desc = "Replaces the drop names with icons. " +
        "§eNote: Will not update the preview above!")
    @ConfigEditorBoolean
    public boolean displayIcons = false;

    @Expose
    @ConfigOption(name = "Only on Barn Plot", desc = "Only shows the overlay while on the Barn plot.")
    @ConfigEditorBoolean
    public boolean onlyOnBarn = true;

    @Expose
    @ConfigLink(owner = DropsStatisticsConfig.class, field = "enabled")
    public Position pos = new Position(5, 20, false, true);
}
