package at.hannibal2.skyhanni.features.fishing.trophy

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.jsonobjects.repo.TrophyFishInfo
import at.hannibal2.skyhanni.data.jsonobjects.repo.TrophyFishJson
import at.hannibal2.skyhanni.events.utils.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.utils.neu.NeuProfileDataLoadedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.StringUtils.splitLines
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle

@SkyHanniModule
object TrophyFishManager {
    private val config get() = SkyHanniMod.feature.fishing.trophyFishing

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<TrophyFishJson>("TrophyFish")
        trophyFishInfo = data.trophyFish
    }

    val fish: MutableMap<String, MutableMap<TrophyRarity, Int>>?
        get() = ProfileStorageData.profileSpecific?.crimsonIsle?.trophyFishes

    private var loadedNeu = false

    @HandleEvent
    fun onNeuProfileDataLoaded(event: NeuProfileDataLoadedEvent) {
        if (loadedNeu || !config.loadFromNeuPV) return

        val caughtTrophyFish = event.getCurrentPlayerData()?.trophyFish?.caught ?: return

        loadedNeu = true

        val savedFishes = fish ?: return
        var changed = false

        val neuData = mutableListOf<Triple<String, TrophyRarity, Int>>()
        for ((fishName, apiAmount) in caughtTrophyFish) {
            val rarity = TrophyRarity.getByName(fishName) ?: continue
            val name = fishName.split("_").dropLast(1).joinToString("")

            val savedFishData = savedFishes.getOrPut(name) { mutableMapOf() }

            val currentSavedAmount = savedFishData[rarity] ?: 0
            neuData.add(Triple(name, rarity, apiAmount))
            if (apiAmount > currentSavedAmount) {
                changed = true
            }
        }
        if (changed) {
            ChatUtils.clickableChat(
                "Click here to load data from NEU PV!", onClick = {
                    updateFromNeuPv(savedFishes, neuData)
                },
                oneTimeClick = true
            )
        }
    }

    private fun updateFromNeuPv(
        savedFishes: MutableMap<String, MutableMap<TrophyRarity, Int>>,
        neuData: MutableList<Triple<String, TrophyRarity, Int>>,
    ) {
        for ((name, rarity, newValue) in neuData) {
            val saved = savedFishes[name] ?: continue

            val current = saved[rarity] ?: 0
            if (newValue > current) {
                saved[rarity] = newValue
                ChatUtils.debug("Updated trophy fishing data from NEU PV:  $name $rarity: $current -> $newValue")
            }
        }
        ChatUtils.chat("Updated Trophy Fishing data via NEU PV!")
    }

    private var trophyFishInfo = mapOf<String, TrophyFishInfo>()

    fun getInfo(internalName: String) = trophyFishInfo[internalName]

    fun getInfoByName(name: String) = trophyFishInfo.values.find { it.displayName == name }

    private fun formatCount(counts: Map<TrophyRarity, Int>, rarity: TrophyRarity): String {
        val count = counts.getOrDefault(rarity, 0)
        return if (count > 0) "§6${count.addSeparators()}" else "§c✖"
    }

    fun TrophyFishInfo.getFilletValue(rarity: TrophyRarity): Int {
        return fillet.getOrDefault(rarity, -1)
    }

    fun TrophyFishInfo.getTooltip(counts: Map<TrophyRarity, Int>): ChatStyle {
        val bestFishObtained = counts.keys.maxOrNull() ?: TrophyRarity.BRONZE
        val rateString = if (rate != null) "§8[§7$rate%§8]" else ""
        val display = """
            |$displayName $rateString
            |${description.splitLines(150)}
            |
            |${TrophyRarity.DIAMOND.formattedString}: ${formatCount(counts, TrophyRarity.DIAMOND)}
            |${TrophyRarity.GOLD.formattedString}: ${formatCount(counts, TrophyRarity.GOLD)}
            |${TrophyRarity.SILVER.formattedString}: ${formatCount(counts, TrophyRarity.SILVER)}
            |${TrophyRarity.BRONZE.formattedString}: ${formatCount(counts, TrophyRarity.BRONZE)}
            |
            |§7Total: ${bestFishObtained.formatCode}${counts.values.sum().addSeparators()}
        """.trimMargin()
        return ChatStyle().setChatHoverEvent(
            HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText(display))
        )
    }
}
