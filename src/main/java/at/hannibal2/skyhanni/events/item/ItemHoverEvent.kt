package at.hannibal2.skyhanni.events.item

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import net.minecraft.item.ItemStack

class ItemHoverEvent(val itemStack: ItemStack, val toolTip: List<String>) : SkyHanniEvent()
