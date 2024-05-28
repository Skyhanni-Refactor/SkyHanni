package at.hannibal2.skyhanni.features.inventory.bazaar

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.compat.hypixel.HypixelWebAPI
import at.hannibal2.skyhanni.events.hypixel.HypixelJoinEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems

@SkyHanniModule
object BazaarDataHolder {

    private var npcPrices = mapOf<NEUInternalName, Double>()

    fun getNpcPrice(internalName: NEUInternalName) = npcPrices[internalName]

    @HandleEvent
    fun onHypixelJoin(event: HypixelJoinEvent) {
        HypixelWebAPI.getItems {
            onSuccess { data ->
                val prices = mutableMapOf<NEUInternalName, Double>()
                val motesPrices = mutableMapOf<NEUInternalName, Double>()

                for (item in data.items) {
                    val neuItemId = NEUItems.transHypixelNameToInternalName(item.id ?: continue)
                    item.npcPrice?.let { prices[neuItemId] = it }
                    item.motesPrice?.let { motesPrices[neuItemId] = it }
                }

                npcPrices = prices
                RiftAPI.motesPrice = motesPrices
            }
            onFailure {
                ErrorManager.logErrorWithData(it, "Error getting npc sell prices")
            }
        }
    }
}
