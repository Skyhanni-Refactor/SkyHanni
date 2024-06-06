package at.hannibal2.skyhanni.config.features.mining;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.AMBER;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.AMETHYST;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.DIAMOND_ESSENCE;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.DOUBLE_POWDER;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.ELECTRON;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.FTX;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.GEMSTONE_POWDER;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.GOLD_ESSENCE;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.JADE;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.MITHRIL_POWDER;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.ROBOTRON;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.RUBY;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.SAPPHIRE;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.SPACER_1;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.SPACER_2;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.TOPAZ;
import static at.hannibal2.skyhanni.config.features.mining.PowderTrackerConfig.PowderDisplayEntry.TOTAL_CHESTS;

public class PowderTrackerConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable the Powder Tracker overlay for mining.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Only when Grinding", desc = "Only show the overlay when powder grinding.")
    @ConfigEditorBoolean
    public boolean onlyWhenPowderGrinding = false;

    @Expose
    @ConfigOption(name = "Great Explorer", desc = "Enable this if your Great Explorer perk is maxed.")
    @ConfigEditorBoolean
    public boolean greatExplorerMaxed = false;

    @Expose
    @ConfigOption(
        name = "Text Format",
        desc = "Drag text to change the appearance of the overlay."
    )
    @ConfigEditorDraggableList
    public Property<List<PowderDisplayEntry>> textFormat = Property.of(new ArrayList<>(Arrays.asList(
        TOTAL_CHESTS,
        DOUBLE_POWDER,
        MITHRIL_POWDER,
        GEMSTONE_POWDER,
        SPACER_1,
        DIAMOND_ESSENCE,
        GOLD_ESSENCE,
        SPACER_2,
        RUBY,
        SAPPHIRE,
        AMBER,
        AMETHYST,
        JADE,
        TOPAZ,
        FTX,
        ELECTRON,
        ROBOTRON
    )));

    public enum PowderDisplayEntry {
        TOTAL_CHESTS("§d852 Total chests Picked §7(950/h)"),
        DOUBLE_POWDER("§bx2 Powder: §aActive!"),
        MITHRIL_POWDER("§b250,420 §aMithril Powder §7(350,000/h)"),
        GEMSTONE_POWDER("§b250,420 §dGemstone Powder §7(350,000/h)"),
        SPACER_1(""),
        DIAMOND_ESSENCE("§b129 §bDiamond Essence §7(600/h)"),
        GOLD_ESSENCE("§b234 §6Gold Essence §7(700/h)"),
        SPACER_2(""),
        RUBY("§50§7-§90§7-§a0§f-0 §cRuby Gemstone"),
        SAPPHIRE("§50§7-§90§7-§a0§f-0 §bSapphire Gemstone"),
        AMBER("§50§7-§90§7-§a0§f-0 §6Amber Gemstone"),
        AMETHYST("§50§7-§90§7-§a0§f-0 §5Amethyst Gemstone"),
        JADE("§50§7-§90§7-§a0§f-0 §aJade Gemstone"),
        TOPAZ("§50§7-§90§7-§a0§f-0 §eTopaz Gemstone"),
        FTX("§b14 §9FTX 3070"),
        ELECTRON("§b14 §9Electron Transmitter"),
        ROBOTRON("§b14 §9Robotron Reflector"),
        SUPERLITE("§b14 §9Superlite Motor"),
        CONTROL_SWITCH("§b14 §9Control Switch"),
        SYNTHETIC_HEART("§b14 §9Synthetic Heart"),
        TOTAL_ROBOT_PARTS("§b14 §9Total Robot Parts"),
        GOBLIN_EGGS("§90§7-§a0§7-§c0§f-§e0§f-§30 §fGoblin Egg"),
        WISHING_COMPASS("§b12 §aWishing Compass"),
        SLUDGE_JUICE("§b320 §aSludge Juice"),
        ASCENSION_ROPE("§b2 §9Ascension Rope"),
        TREASURITE("§b6 §5Treasurite"),
        JUNGLE_HEART("§b4 §6Jungle Heart"),
        PICKONIMBUS("§b1 §5Pickonimbus 2000"),
        YOGGIE("§b14 §aYoggie"),
        PREHISTORIC_EGG("§b9 §fPrehistoric Egg"),
        OIL_BARREL("§b25 §aOil Barrel"),
        ;

        private final String str;

        PowderDisplayEntry(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    @Expose
    @ConfigLink(owner = PowderTrackerConfig.class, field = "enabled")
    public Position position = new Position(-274, 0, false, true);

}
