package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.entity.CheckRenderEntityEvent
import net.minecraft.entity.item.EntityXPOrb

object ExpOrbsOnGroundHider {

    @HandleEvent(onlyOnSkyblock = true)
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (!SkyHanniMod.feature.misc.hideExpBottles) return

        if (event.entity is EntityXPOrb) {
            event.cancel()
        }
    }
}
