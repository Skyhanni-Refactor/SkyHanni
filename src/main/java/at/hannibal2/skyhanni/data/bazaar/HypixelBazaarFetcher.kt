package at.hannibal2.skyhanni.data.bazaar

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.HypixelAPI
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.compat.hypixel.HypixelWebAPI
import at.hannibal2.skyhanni.compat.hypixel.data.HypixelBazaar
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarData
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.NEUItems.getItemStackOrNull
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

// https://api.hypixel.net/#tag/SkyBlock/paths/~1v2~1skyblock~1bazaar/get
@SkyHanniModule
object HypixelBazaarFetcher {
    private const val HIDDEN_FAILED_ATTEMPTS = 3

    var latestProductInformation = mapOf<NEUInternalName, BazaarData>()
    private var nextFetchTime = SimpleTimeMark.farPast()
    private var failedAttempts = 0
    private var nextFetchIsManual = false

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!canFetch()) return
        SkyHanniMod.coroutineScope.launch {
            fetchAndProcessBazaarData()
        }
    }

    private suspend fun fetchAndProcessBazaarData() {
        nextFetchTime = SimpleTimeMark.now() + 2.minutes
        val fetchType = if (nextFetchIsManual) "manual" else "automatic"
        nextFetchIsManual = false

        HypixelWebAPI.getBazaar().fold(
            onSuccess = { data ->
                if (data.success) {
                    latestProductInformation = process(data.products)
                    failedAttempts = 0
                }
            },
            onFailure = {
                onError(fetchType, it)
            }
        )
    }

    private fun process(products: Map<String, HypixelBazaar.Product>) = products.mapNotNull { (key, product) ->
        val internalName = NEUItems.transHypixelNameToInternalName(key)
        val sellOfferPrice = product.buySummary.minOfOrNull { it.pricePerUnit } ?: 0.0
        val insantBuyPrice = product.sellSummary.maxOfOrNull { it.pricePerUnit } ?: 0.0

        if (product.quickStatus.isEmpty()) {
            return@mapNotNull null
        }

        if (internalName.getItemStackOrNull() == null) {
            // Items that exist in Hypixel's Bazaar API, but not in NEU repo (not visible in the ingame bazaar).
            // Should only include Enchants
            if (LorenzUtils.debug)
                println("Unknown bazaar product: $key/$internalName")
            return@mapNotNull null
        }
        internalName to BazaarData(internalName.itemName, sellOfferPrice, insantBuyPrice, product)
    }.toMap()

    private fun onError(fetchType: String, e: Throwable, rawResponse: String? = null) {
        val userMessage = "Failed fetching bazaar price data from hypixel"
        failedAttempts++
        if (failedAttempts <= HIDDEN_FAILED_ATTEMPTS) {
            nextFetchTime = SimpleTimeMark.now() + 15.seconds
            ChatUtils.debug("$userMessage. (errorMessage=${e.message}, failedAttepmts=$failedAttempts, $fetchType")
            e.printStackTrace()
        } else {
            nextFetchTime = SimpleTimeMark.now() + 15.minutes
            ErrorManager.logErrorWithData(
                e,
                userMessage,
                "fetchType" to fetchType,
                "failedAttepmts" to failedAttempts,
                "rawResponse" to rawResponse
            )
        }
    }

    fun fetchNow() {
        failedAttempts = 0
        nextFetchIsManual = true
        nextFetchTime = SimpleTimeMark.now()
        ChatUtils.chat("Manually updating the bazaar prices right now..")
    }

    private fun canFetch() = HypixelAPI.onHypixel && nextFetchTime.isInPast()
}
