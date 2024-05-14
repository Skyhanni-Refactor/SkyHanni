package at.hannibal2.skyhanni.features.fishing.trophy

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.features.fishing.FishingAPI
import at.hannibal2.skyhanni.test.GriffinUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.mc.McPlayer
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object OdgerWaypoint {

    private val config get() = SkyHanniMod.feature.fishing.trophyFishing
    private val location = LorenzVec(-373, 207, -808)

    private var trophyFishInInventory = false

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled() || !event.isMod(10)) return
        trophyFishInInventory = McPlayer.has(ItemCategory.TROPHY_FISH)
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return
        if (FishingAPI.holdingLavaRod) return
        if (!trophyFishInInventory) return

        event.drawWaypointFilled(location, LorenzColor.WHITE.toColor())
        event.drawDynamicText(location, "Odger", 1.5)
    }
    fun isEnabled() = IslandType.CRIMSON_ISLE.isInIsland() && config.odgerLocation
}
