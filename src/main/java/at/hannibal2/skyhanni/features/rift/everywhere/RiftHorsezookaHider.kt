package at.hannibal2.skyhanni.features.rift.everywhere

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.entity.CheckRenderEntityEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.utils.InventoryUtils
import net.minecraft.entity.passive.EntityHorse

object RiftHorsezookaHider {

    @HandleEvent(onlyOnIsland = IslandType.THE_RIFT)
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (!SkyHanniMod.feature.rift.horsezookaHider) return

        if (event.entity is EntityHorse && InventoryUtils.itemInHandId.equals("HORSEZOOKA")) {
            event.cancel()
        }
    }
}
