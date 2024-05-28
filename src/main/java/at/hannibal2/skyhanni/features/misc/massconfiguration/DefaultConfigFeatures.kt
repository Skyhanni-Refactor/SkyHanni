package at.hannibal2.skyhanni.features.misc.massconfiguration

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigFileType
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import io.github.notenoughupdates.moulconfig.processor.ConfigProcessorDriver
import net.minecraft.client.Minecraft
import net.minecraft.command.CommandBase

@SkyHanniModule
object DefaultConfigFeatures {

    private var didNotifyOnce = false

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (didNotifyOnce) return
        Minecraft.getMinecraft().thePlayer ?: return
        didNotifyOnce = true

        val knownToggles = SkyHanniMod.knownFeaturesData.knownFeatures
        val updated = SkyHanniMod.version !in knownToggles
        val processor = FeatureToggleProcessor()
        ConfigProcessorDriver(processor).processConfig(SkyHanniMod.feature)
        knownToggles[SkyHanniMod.version] = processor.allOptions.map { it.path }
        SkyHanniMod.configManager.saveConfig(ConfigFileType.KNOWN_FEATURES, "Updated known feature flags")
        if (!SkyHanniMod.feature.storage.hasPlayedBefore) {
            SkyHanniMod.feature.storage.hasPlayedBefore = true
            ChatUtils.clickableChat(
                "Looks like this is the first time you are using SkyHanni. " +
                    "Click here to configure default options, or run /shdefaultoptions.",
                onClick = {
                    onCommand("null", "null")
                }
            )
        } else if (updated) {
            val lastVersion = knownToggles.keys.last { it != SkyHanniMod.version }
            val command = "/shdefaultoptions $lastVersion ${SkyHanniMod.version}"
            ChatUtils.clickableChat(
                "Looks like you updated SkyHanni. " +
                    "Click here to configure the newly introduced options, or run $command.",
                onClick = {
                    onCommand(lastVersion, SkyHanniMod.version)
                }
            )
        }
    }

    fun onCommand(old: String, new: String) {
        val processor = FeatureToggleProcessor()
        ConfigProcessorDriver(processor).processConfig(SkyHanniMod.feature)
        var optionList = processor.orderedOptions
        val knownToggles = SkyHanniMod.knownFeaturesData.knownFeatures
        val togglesInNewVersion = knownToggles[new]
        if (new != "null" && togglesInNewVersion == null) {
            ChatUtils.chat("Unknown version $new")
            return
        }
        val togglesInOldVersion = knownToggles[old]
        if (old != "null" && togglesInOldVersion == null) {
            ChatUtils.chat("Unknown version $old")
            return
        }
        optionList = optionList
            .mapValues { it ->
                it.value.filter {
                    (togglesInNewVersion == null || it.path in togglesInNewVersion) &&
                        (togglesInOldVersion == null || it.path !in togglesInOldVersion)
                }
            }
            .filter { (_, filteredOptions) -> filteredOptions.isNotEmpty() }
        if (optionList.isEmpty()) {
            ChatUtils.chat("There are no new options to configure between $old and $new")
            return
        }
        SkyHanniMod.screenToOpen = DefaultConfigOptionGui(optionList, old, new)
    }

    fun applyCategorySelections(
        resetSuggestionState: MutableMap<Category, ResetSuggestionState>,
        orderedOptions: Map<Category, List<FeatureToggleableOption>>,
    ) {
        orderedOptions.forEach { (cat, options) ->
            for (option in options) {
                val resetState = option.toggleOverride ?: resetSuggestionState[cat]!!
                if (resetState == ResetSuggestionState.LEAVE_DEFAULTS) continue
                val onState = option.isTrueEnabled
                val setTo = if (resetState == ResetSuggestionState.TURN_ALL_ON) {
                    onState
                } else {
                    !onState
                }
                option.setter(setTo)
            }
        }
    }

    fun onComplete(strings: Array<String>): List<String> {
        if (strings.size <= 2)
            return CommandBase.getListOfStringsMatchingLastWord(
                strings,
                SkyHanniMod.knownFeaturesData.knownFeatures.keys + listOf("null")
            )
        return listOf()
    }
}
