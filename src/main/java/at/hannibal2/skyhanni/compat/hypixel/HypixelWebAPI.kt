package at.hannibal2.skyhanni.compat.hypixel

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.compat.hypixel.data.HypixelBazaar
import at.hannibal2.skyhanni.compat.hypixel.data.HypixelMayor
import at.hannibal2.skyhanni.compat.hypixel.data.HypixelSkyblockItems
import at.hannibal2.skyhanni.utils.http.Http
import at.hannibal2.skyhanni.utils.json.BaseGsonBuilder
import kotlinx.coroutines.launch

object HypixelWebAPI {

    private val gson by lazy {
        BaseGsonBuilder.gson()
            .create()
    }

    fun getElection(handler: suspend Result<HypixelMayor>.() -> Unit) {
        SkyHanniMod.coroutineScope.launch {
            Http.getResult<HypixelMayor>(
                url = "https://api.hypixel.net/v2/resources/skyblock/election",
                gson = gson,
                errorFactory = ::HypixelError
            ).handler()
        }
    }

    fun getItems(handler: suspend Result<HypixelSkyblockItems>.() -> Unit) {
        SkyHanniMod.coroutineScope.launch {
            Http.getResult<HypixelSkyblockItems>(
                url = "https://api.hypixel.net/v2/resources/skyblock/items",
                gson = gson,
                errorFactory = ::HypixelError
            ).handler()
        }
    }

    suspend fun getBazaar() = Http.getResult<HypixelBazaar>(
        url = "https://api.hypixel.net/v2/skyblock/bazaar",
        gson = gson,
        errorFactory = ::HypixelError
    )
}
