package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getItemUuid
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ShowItemUuid {

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!SkyHanniMod.feature.dev.debug.showItemUuid) return
        event.itemStack.getItemUuid()?.let {
            event.toolTip.add("§7Item UUID: '$it'")
        }
    }
}
