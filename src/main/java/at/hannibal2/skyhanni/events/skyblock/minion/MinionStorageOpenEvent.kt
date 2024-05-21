package at.hannibal2.skyhanni.events.skyblock.minion

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.utils.LorenzVec
import net.minecraft.item.ItemStack

class MinionStorageOpenEvent(val position: LorenzVec?, val inventoryItems: Map<Int, ItemStack>) : SkyHanniEvent()
