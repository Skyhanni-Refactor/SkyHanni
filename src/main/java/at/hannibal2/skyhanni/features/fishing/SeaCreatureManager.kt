package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.data.jsonobjects.repo.SeaCreatureJson
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.fishing.SeaCreatureFishEvent
import at.hannibal2.skyhanni.events.utils.RepositoryReloadEvent

object SeaCreatureManager {

    private var doubleHook = false

    private val seaCreatureMap = mutableMapOf<String, SeaCreature>()
    var allFishingMobs = mapOf<String, SeaCreature>()
    var allVariants = mapOf<String, List<String>>()

    // TODO repo pattern
    private val doubleHookMessages = setOf(
        "§eIt's a §r§aDouble Hook§r§e! Woot woot!",
        "§eIt's a §r§aDouble Hook§r§e!"
    )

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!SkyBlockAPI.isConnected) return
        if (doubleHookMessages.contains(event.message)) {
            if (SkyHanniMod.feature.fishing.compactDoubleHook) {
                event.blockedReason = "double_hook"
            }
            doubleHook = true
        } else {
            val seaCreature = getSeaCreature(event.message)
            if (seaCreature != null) {
                SeaCreatureFishEvent(seaCreature, event, doubleHook).post()
            }
            doubleHook = false
        }
    }

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        seaCreatureMap.clear()
        allFishingMobs = emptyMap()
        var counter = 0

        val data = event.getConstant<Map<String, SeaCreatureJson>>("SeaCreatures", SeaCreatureJson.TYPE)
        val allFishingMobs = mutableMapOf<String, SeaCreature>()

        val variants = mutableMapOf<String, List<String>>()

        for ((variantName, variant) in data) {
            val chatColor = variant.chatColor
            val variantFishes = mutableListOf<String>()
            variants[variantName] = variantFishes
            for ((name, seaCreature) in variant.seaCreatures) {
                val chatMessage = seaCreature.chatMessage
                val fishingExperience = seaCreature.fishingExperience
                val rarity = seaCreature.rarity
                val rare = seaCreature.rare

                val creature = SeaCreature(name, fishingExperience, chatColor, rare, rarity)
                seaCreatureMap[chatMessage] = creature
                allFishingMobs[name] = creature
                variantFishes.add(name)
                counter++
            }
        }
        this.allFishingMobs = allFishingMobs
        allVariants = variants
    }

    private fun getSeaCreature(message: String): SeaCreature? {
        return seaCreatureMap.getOrDefault(message, null)
    }
}
