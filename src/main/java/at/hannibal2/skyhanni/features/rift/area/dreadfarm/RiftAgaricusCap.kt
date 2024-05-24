package at.hannibal2.skyhanni.features.rift.area.dreadfarm

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandArea
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.datetime.TimeUtils.format
import at.hannibal2.skyhanni.utils.mc.McPlayer
import at.hannibal2.skyhanni.utils.mc.McWorld.getBlockStateAt

object RiftAgaricusCap {

    private val config get() = RiftAPI.config.area.dreadfarm
    private var startTime = SimpleTimeMark.farPast()
    private var location: LorenzVec? = null

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!isEnabled()) return
        if (!IslandArea.WEST_VILLAGE.isInside() && !IslandArea.DREADFARM.isInside()) return

        location = updateLocation()
    }

    private fun updateLocation(): LorenzVec? {
        if (McPlayer.heldItem?.getInternalName() != SkyhanniItems.FARMING_WAND()) return null
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

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
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
