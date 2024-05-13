package at.hannibal2.skyhanni.features.rift.area.dreadfarm

import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.datetime.TimeUtils.format
import at.hannibal2.skyhanni.utils.mc.McPlayer
import at.hannibal2.skyhanni.utils.mc.McWorld.getBlockStateAt
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class RiftAgaricusCap {

    private val config get() = RiftAPI.config.area.dreadfarm
    private var startTime = SimpleTimeMark.farPast()
    private var location: LorenzVec? = null

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        val area = LorenzUtils.skyBlockArea
        if (area != "West Village" && area != "Dreadfarm") return

        location = updateLocation()
    }

    private fun updateLocation(): LorenzVec? {
        if (McPlayer.heldItem?.getInternalName() != RiftAPI.farmingTool) return null
        val currentLocation = McPlayer.blockLookingAt ?: return null

        when (currentLocation.getBlockStateAt().toString()) {
            "minecraft:brown_mushroom" -> {
                return if (location != currentLocation) {
                    startTime = SimpleTimeMark.now()
                    currentLocation
                } else {
                    if (startTime.isFarFuture()) {
                        startTime = SimpleTimeMark.now()
                    }
                    location
                }
            }

            "minecraft:red_mushroom" -> {
                if (location == currentLocation) {
                    startTime = SimpleTimeMark.farFuture()
                    return location
                }
            }
        }
        return null
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return

        val location = location?.add(y = 0.6) ?: return

        if (startTime.isFarFuture()) {
            event.drawDynamicText(location, "§cClick!", 1.5)
            return
        }

        val format = startTime.passedSince().format(showMilliSeconds = true)
        event.drawDynamicText(location, "§b$format", 1.5)
    }

    fun isEnabled() = RiftAPI.inRift() && config.agaricusCap
}
