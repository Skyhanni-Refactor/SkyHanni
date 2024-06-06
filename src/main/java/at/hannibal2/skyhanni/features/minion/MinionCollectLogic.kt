package at.hannibal2.skyhanni.features.minion

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.inventory.ItemAddInInventoryEvent
import at.hannibal2.skyhanni.events.skyblock.minion.MinionOpenEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.mc.McPlayer

@SkyHanniModule
object MinionCollectLogic {

    private var oldMap = mapOf<NEUInternalName, Int>()

    @HandleEvent
    fun onMinionOpen(event: MinionOpenEvent) {
        if (oldMap.isNotEmpty()) return
        oldMap = count()
    }

    private fun count(): MutableMap<NEUInternalName, Int> {
        val map = mutableMapOf<NEUInternalName, Int>()
        for (stack in McPlayer.inventory) {
            val internalName = stack.getInternalName()
            val (newId, amount) = NEUItems.getPrimitiveMultiplier(internalName)
            val old = map[newId] ?: 0
            map[newId] = old + amount * stack.stackSize
        }
        return map
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        closeMinion()
    }

    private fun closeMinion() {
        if (oldMap.isEmpty()) return

        for ((internalId, amount) in count()) {
            val old = oldMap[internalId] ?: 0
            val diff = amount - old

            if (diff > 0) {
                ItemAddInInventoryEvent(internalId, diff).post()
            }
        }

        oldMap = emptyMap()
    }
}
