package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.utils.LorenzUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

object GardenBurrowingSporesNotifier {

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!GardenAPI.inGarden()) return
        if (!GardenAPI.config.burrowingSporesNotification) return

        if (event.message.endsWith("§6§lVERY RARE CROP! §r§f§r§9Burrowing Spores")) {
            LorenzUtils.sendTitle("§9Burrowing Spores!", 5.seconds)
        }
    }
}
