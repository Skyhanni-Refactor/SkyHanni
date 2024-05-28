package at.hannibal2.skyhanni.features.inventory.bazaar

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.data.jsonobjects.other.SkyblockItemsDataJson
import at.hannibal2.skyhanni.events.hypixel.HypixelJoinEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.APIUtil
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.json.fromJson
import kotlinx.coroutines.launch

@SkyHanniModule
object BazaarDataHolder {

    private var npcPrices = mapOf<NEUInternalName, Double>()

    fun getNpcPrice(internalName: NEUInternalName) = npcPrices[internalName]

    @HandleEvent
    fun onHypixelJoin(event: HypixelJoinEvent) {
        SkyHanniMod.coroutineScope.launch {
            npcPrices = loadNpcPrices()
        }
    }

    private fun loadNpcPrices(): MutableMap<NEUInternalName, Double> {
        val list = mutableMapOf<NEUInternalName, Double>()
        val apiResponse = APIUtil.getJSONResponse("https://api.hypixel.net/v2/resources/skyblock/items")
        try {
            val itemsData = ConfigManager.gson.fromJson<SkyblockItemsDataJson>(apiResponse)

            val motesPrice = mutableMapOf<NEUInternalName, Double>()
            for (item in itemsData.items) {
                val neuItemId = NEUItems.transHypixelNameToInternalName(item.id ?: continue)
                item.npcPrice?.let { list[neuItemId] = it }
                item.motesPrice?.let { motesPrice[neuItemId] = it }
            }
            RiftAPI.motesPrice = motesPrice
        } catch (e: Throwable) {
            ErrorManager.logErrorWithData(
                e, "Error getting npc sell prices",
                "hypixelApiResponse" to apiResponse
            )
        }
        return list
    }
}
