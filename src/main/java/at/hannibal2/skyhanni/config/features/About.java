package at.hannibal2.skyhanni.config.features;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.features.misc.update.ConfigVersionDisplay;
import at.hannibal2.skyhanni.utils.system.OS;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class About {

    @ConfigOption(name = "Current Version", desc = "This is the SkyHanni version you are running currently")
    @ConfigVersionDisplay
    public transient Void currentVersion = null;

    @ConfigOption(name = "Auto Updates", desc = "Automatically check for updates on each startup")
    @Expose
    @ConfigEditorBoolean
    public boolean autoUpdates = true;

    @ConfigOption(name = "Full Auto Updates", desc = "Automatically downloads new version on each startup.")
    @Expose
    @FeatureToggle
    @ConfigEditorBoolean
    public boolean fullAutoUpdates = false;

    @ConfigOption(name = "Update Stream", desc = "How frequently do you want updates for SkyHanni")
    @Expose
    @ConfigEditorDropdown
    public Property<UpdateStream> updateStream = Property.of(UpdateStream.RELEASES);


    @ConfigOption(name = "Used Software", desc = "Information about used software and licenses")
    @Accordion
    @Expose
    public Licenses licenses = new Licenses();

    public enum UpdateStream {
        NONE("None", "none"),
        BETA("Beta", "pre"),
        RELEASES("Full", "full");

        private final String label;
        private final String stream;

        UpdateStream(String label, String stream) {
            this.label = label;
            this.stream = stream;
        }

        public String getStream() {
            return stream;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public static class Licenses {

        @ConfigOption(name = "MoulConfig", desc = "MoulConfig is available under the LGPL 3.0 License or later version")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable moulConfig = () -> OS.openUrl("https://github.com/NotEnoughUpdates/MoulConfig");

        @ConfigOption(name = "NotEnoughUpdates", desc = "NotEnoughUpdates is available under the LGPL 3.0 License or later version")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable notEnoughUpdates = () -> OS.openUrl("https://github.com/NotEnoughUpdates/NotEnoughUpdates");

        @ConfigOption(name = "Forge", desc = "Forge is available under the LGPL 3.0 license")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable forge = () -> OS.openUrl("https://github.com/MinecraftForge/MinecraftForge");

        @ConfigOption(name = "LibAutoUpdate", desc = "LibAutoUpdate is available under the BSD 2 Clause License")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable libAutoUpdate = () -> OS.openUrl("https://git.nea.moe/nea/libautoupdate/");

        @ConfigOption(name = "Mixin", desc = "Mixin is available under the MIT License")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable mixin = () -> OS.openUrl("https://github.com/SpongePowered/Mixin/");

        @ConfigOption(name = "DiscordIPC", desc = "DiscordIPC is available under the Apache License 2.0")
        @ConfigEditorButton(buttonText = "GitHub")
        public Runnable discordRPC = () -> OS.openUrl("https://github.com/jagrosh/DiscordIPC");
    }
}
