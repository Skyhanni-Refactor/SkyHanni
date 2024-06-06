package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.entity.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzUtils
import net.minecraft.entity.boss.EntityWither
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import at.hannibal2.skyhanni.utils.mc.McWorld

@SkyHanniModule
object HideFarEntities {
    private val config get() = SkyHanniMod.feature.misc.hideFarEntities

    private var ignored = emptySet<Int>()

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!isEnabled()) return

        val maxAmount = config.maxAmount.coerceAtLeast(1)
        val minDistance = config.minDistance.coerceAtLeast(3)

        ignored = McWorld.entities
            .map { it.entityId to it.distanceToPlayer() }
            .filter { it.second > minDistance }
            .sortedBy { it.second }.drop(maxAmount)
            .map { it.first }.toSet()
    }

    @HandleEvent
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        val entity = event.entity
        if (entity is EntityWither && entity.entityId < 0) return
        if (isEnabled() && entity.entityId in ignored) {
            event.cancel()
        }
    }

    fun isEnabled() = SkyBlockAPI.isConnected && config.enabled && !(GardenAPI.inGarden() && config.excludeGarden)
}
