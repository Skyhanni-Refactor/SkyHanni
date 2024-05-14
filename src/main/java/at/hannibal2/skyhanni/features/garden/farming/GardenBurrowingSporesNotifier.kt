package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

object GardenBurrowingSporesNotifier {

    // TODO use a repo pattern
    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!GardenAPI.inGarden()) return
        if (!GardenAPI.config.burrowingSporesNotification) return

        if (event.message.endsWith("§6§lVERY RARE CROP! §r§f§r§9Burrowing Spores")) {
            TitleManager.sendTitle("§9Burrowing Spores!", 5.seconds)
        }
    }
}
