package at.hannibal2.skyhanni.config.features.garden.visitor;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class VisitorConfig {
    @Expose
    @ConfigOption(name = "Visitor Timer", desc = "")
    @Accordion
    public TimerConfig timer = new TimerConfig();

    @Expose
    @ConfigOption(name = "Visitor Shopping List", desc = "")
    @Accordion
    public ShoppingListConfig shoppingList = new ShoppingListConfig();

    @Expose
    @ConfigOption(name = "Visitor Inventory", desc = "")
    @Accordion
    public InventoryConfig inventory = new InventoryConfig();

    @Expose
    @ConfigOption(name = "Visitor Reward Warning", desc = "")
    @Accordion
    public RewardWarningConfig rewardWarning = new RewardWarningConfig();

    @Expose
    @ConfigOption(name = "Notification Chat", desc = "Show in chat when a new visitor is visiting your island.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean notificationChat = true;

    @Expose
    @ConfigOption(name = "Notification Title", desc = "Show a title when a new visitor is visiting your island.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean notificationTitle = true;

    @Expose
    @ConfigOption(name = "Highlight Status", desc = "Highlight the status for visitors with a text above or with color.")
    @ConfigEditorDropdown
    public HighlightMode highlightStatus = HighlightMode.BOTH;

    public enum HighlightMode {
        COLOR("Color Only"),
        NAME("Name Only"),
        BOTH("Both"),
        DISABLED("Disabled");
        private final String str;

        HighlightMode(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    @Expose
    @ConfigOption(name = "Hypixel Message", desc = "Hide the chat message from Hypixel that a new visitor has arrived at your garden.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hypixelArrivedMessage = true;

    @Expose
    @ConfigOption(name = "Hide Chat", desc = "Hide chat messages from the visitors in the garden. (Except Beth and Spaceman)")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideChat = true;

    @Expose
    @ConfigOption(name = "Visitor Drops Statistics Counter", desc = "")
    @Accordion
    public DropsStatisticsConfig dropsStatistics = new DropsStatisticsConfig();

    @Expose
    @ConfigOption(
        name = "Accept Hotkey",
        desc = "Accept a visitor when you press this keybind while in the visitor GUI. " +
            "§eUseful for getting Ephemeral Gratitudes during the 2023 Halloween event."
    )
    @ConfigEditorKeybind(
        defaultKey = Keyboard.KEY_NONE
    )
    public int acceptHotkey = Keyboard.KEY_NONE;


    @Expose
    @ConfigOption(
        name = "Highlight Visitors in SkyBlock",
        desc = "Highlights Visitors outside of the Garden"
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean highlightVisitors = false;

    @Expose
    @ConfigOption(
        name = "Block Interacting with Visitors",
        desc = "Blocks you from interacting with / unlocking Visitors to allow for Dedication Cycling"
    )
    @ConfigEditorDropdown
    public VisitorBlockBehaviour blockInteracting = VisitorBlockBehaviour.DONT;

    public enum VisitorBlockBehaviour {
        DONT("Don't"), ALWAYS("Always"), ONLY_ON_BINGO("Only on Bingo");

        final String str;

        VisitorBlockBehaviour(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

}
