package at.hannibal2.skyhanni.compat.hypixel

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.compat.hypixel.data.HypixelBazaar
import at.hannibal2.skyhanni.compat.hypixel.data.HypixelMayor
import at.hannibal2.skyhanni.compat.hypixel.data.HypixelSkyblockItems
import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.utils.http.Http
import kotlinx.coroutines.launch

object HypixelWebAPI {

    private val gson by lazy {
        ConfigManager.createBaseGsonBuilder()
            .create()
    }

    fun getElection(handler: suspend Result<HypixelMayor>.() -> Unit) {
        SkyHanniMod.coroutineScope.launch {
            handler(Http.getResult<HypixelMayor>(
                url = "https://api.hypixel.net/v2/resources/skyblock/election",
                gson = gson,
                errorFactory = ::HypixelError
            ))
        }
    }

    fun getItems(handler: suspend Result<HypixelSkyblockItems>.() -> Unit) {
        SkyHanniMod.coroutineScope.launch {
            handler(Http.getResult<HypixelSkyblockItems>(
                url = "https://api.hypixel.net/v2/resources/skyblock/items",
                gson = gson,
                errorFactory = ::HypixelError
            ))
        }
    }

    suspend fun getBazaar() = Http.getResult<HypixelBazaar>(
        url = "https://api.hypixel.net/v2/skyblock/bazaar",
        gson = gson,
        errorFactory = ::HypixelError
    )
}
