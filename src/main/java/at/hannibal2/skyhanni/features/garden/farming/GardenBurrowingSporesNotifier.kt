package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GardenBurrowingSporesNotifier {

    // TODO use a repo pattern
    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onChat(event: SkyHanniChatEvent) {
        if (!GardenAPI.config.burrowingSporesNotification) return

        if (event.message.endsWith("§6§lVERY RARE CROP! §r§f§r§9Burrowing Spores")) {
            TitleManager.sendTitle("§9Burrowing Spores!", 5.seconds)
        }
    }
}
