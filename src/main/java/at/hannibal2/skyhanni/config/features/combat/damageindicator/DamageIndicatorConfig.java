package at.hannibal2.skyhanni.config.features.combat.damageindicator;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static at.hannibal2.skyhanni.config.features.combat.damageindicator.DamageIndicatorConfig.BossCategory.ARACHNE;
import static at.hannibal2.skyhanni.config.features.combat.damageindicator.DamageIndicatorConfig.BossCategory.DIANA_MOBS;
import static at.hannibal2.skyhanni.config.features.combat.damageindicator.DamageIndicatorConfig.BossCategory.GARDEN_PESTS;
import static at.hannibal2.skyhanni.config.features.combat.damageindicator.DamageIndicatorConfig.BossCategory.INFERNO_DEMONLORD;
import static at.hannibal2.skyhanni.config.features.combat.damageindicator.DamageIndicatorConfig.BossCategory.NETHER_MINI_BOSSES;
import static at.hannibal2.skyhanni.config.features.combat.damageindicator.DamageIndicatorConfig.BossCategory.REINDRAKE;
import static at.hannibal2.skyhanni.config.features.combat.damageindicator.DamageIndicatorConfig.BossCategory.REVENANT_HORROR;
import static at.hannibal2.skyhanni.config.features.combat.damageindicator.DamageIndicatorConfig.BossCategory.RIFTSTALKER_BLOODFIEND;
import static at.hannibal2.skyhanni.config.features.combat.damageindicator.DamageIndicatorConfig.BossCategory.SEA_CREATURES;
import static at.hannibal2.skyhanni.config.features.combat.damageindicator.DamageIndicatorConfig.BossCategory.SVEN_PACKMASTER;
import static at.hannibal2.skyhanni.config.features.combat.damageindicator.DamageIndicatorConfig.BossCategory.TARANTULA_BROODFATHER;
import static at.hannibal2.skyhanni.config.features.combat.damageindicator.DamageIndicatorConfig.BossCategory.THE_RIFT_BOSSES;
import static at.hannibal2.skyhanni.config.features.combat.damageindicator.DamageIndicatorConfig.BossCategory.VANQUISHER;
import static at.hannibal2.skyhanni.config.features.combat.damageindicator.DamageIndicatorConfig.BossCategory.VOIDGLOOM_SERAPH;

public class DamageIndicatorConfig {

    @Expose
    @ConfigOption(name = "Damage Indicator Enabled", desc = "Show the boss' remaining health.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Healing Chat Message", desc = "Sends a chat message when a boss heals themself.")
    @ConfigEditorBoolean
    public boolean healingMessage = false;

    @Expose
    @ConfigOption(
        name = "Boss Name",
        desc = "Change how the boss name should be displayed.")
    @ConfigEditorDropdown
    public NameVisibility bossName = NameVisibility.FULL_NAME;

    public enum NameVisibility {
        HIDDEN("Hidden"),
        FULL_NAME("Full Name"),
        SHORT_NAME("Short Name"),
        ;

        private final String str;

        NameVisibility(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    @Expose
    @ConfigOption(
        name = "Select Boss",
        desc = "Change what type of boss you want the damage indicator be enabled for."
    )
    @ConfigEditorDraggableList
    //TODO only show currently working and tested features
    public List<BossCategory> bossesToShow = new ArrayList<>(Arrays.asList(
        NETHER_MINI_BOSSES,
        VANQUISHER,
        REVENANT_HORROR,
        TARANTULA_BROODFATHER,
        SVEN_PACKMASTER,
        VOIDGLOOM_SERAPH,
        INFERNO_DEMONLORD,
        DIANA_MOBS,
        SEA_CREATURES,
        ARACHNE,
        THE_RIFT_BOSSES,
        RIFTSTALKER_BLOODFIEND,
        REINDRAKE,
        GARDEN_PESTS

    ));

    public enum BossCategory {
        NETHER_MINI_BOSSES("§bNether Mini Bosses"),
        VANQUISHER("§bVanquisher"),
        ENDERSTONE_PROTECTOR("§bEndstone Protector (not tested)"),
        ENDER_DRAGON("§bEnder Dragon (not finished)"),
        REVENANT_HORROR("§bRevenant Horror"),
        TARANTULA_BROODFATHER("§bTarantula Broodfather"),
        SVEN_PACKMASTER("§bSven Packmaster"),
        VOIDGLOOM_SERAPH("§bVoidgloom Seraph"),
        INFERNO_DEMONLORD("§bInferno Demonlord"),
        HEADLESS_HORSEMAN("§bHeadless Horseman (bugged)"),
        DUNGEON_FLOOR_1("§bDungeon Floor 1"),
        DUNGEON_FLOOR_2("§bDungeon Floor 2"),
        DUNGEON_FLOOR_3("§bDungeon Floor 3"),
        DUNGEON_FLOOR_4("§bDungeon Floor 4"),
        DUNGEON_FLOOR_5("§bDungeon Floor 5"),
        DUNGEON_FLOOR_6("§bDungeon Floor 6"),
        DUNGEON_FLOOR_7("§bDungeon Floor 7"),
        DIANA_MOBS("§bDiana Mobs"),
        SEA_CREATURES("§bSea Creatures"),
        DUMMY("Dummy"),
        ARACHNE("§bArachne"),
        THE_RIFT_BOSSES("§bThe Rift Bosses"),
        RIFTSTALKER_BLOODFIEND("§bRiftstalker Bloodfiend"),
        REINDRAKE("§6Reindrake"),
        GARDEN_PESTS("§aGarden Pests"),
        ;

        private final String str;

        BossCategory(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    @Expose
    @ConfigOption(name = "Hide Damage Splash", desc = "Hiding damage splashes near the damage indicator.")
    @ConfigEditorBoolean
    public boolean hideDamageSplash = false;

    @Expose
    @ConfigOption(name = "Damage Over Time", desc = "Show damage and health over time below the damage indicator.")
    @ConfigEditorBoolean
    public boolean showDamageOverTime = false;

    @Expose
    @ConfigOption(name = "Hide Nametag", desc = "Hide the vanilla nametag of damage indicator bosses.")
    @ConfigEditorBoolean
    public boolean hideVanillaNametag = false;

    @Expose
    @ConfigOption(name = "Time to Kill", desc = "Show the time it takes to kill the slayer boss.")
    @ConfigEditorBoolean
    public boolean timeToKillSlayer = true;


    @Expose
    @ConfigOption(name = "Ender Slayer", desc = "")
    @Accordion
    public EnderSlayerConfig enderSlayer = new EnderSlayerConfig();

    @Expose
    @ConfigOption(name = "Vampire Slayer", desc = "")
    @Accordion
    public VampireSlayerConfig vampireSlayer = new VampireSlayerConfig();
}
