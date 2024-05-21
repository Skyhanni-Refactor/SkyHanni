package at.hannibal2.skyhanni.features.rift.area.wyldwoods

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.mc.McWorld
import kotlin.time.Duration.Companion.milliseconds

object ShyCruxWarnings {

    private val config get() = RiftAPI.config.area.wyldWoods
    private val shyNames = arrayOf("I'm ugly! :(", "Eek!", "Don't look at me!", "Look away!")

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!RiftAPI.inRift() || !config.shyWarning) return
        if (McWorld.entities.any { it.name in shyNames && it.distanceToPlayer() < 8 }) {
            TitleManager.sendTitle("§eLook away!", 150.milliseconds)
        }
    }
}
