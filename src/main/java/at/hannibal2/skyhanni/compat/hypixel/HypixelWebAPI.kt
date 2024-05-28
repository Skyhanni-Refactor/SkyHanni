package at.hannibal2.skyhanni.compat.hypixel

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.compat.hypixel.data.HypixelMayorData
import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.utils.http.Http
import kotlinx.coroutines.launch

object HypixelWebAPI {

    private val gson by lazy {
        ConfigManager.createBaseGsonBuilder()
            .create()
    }

    fun getElection(handler: suspend Result<HypixelMayorData>.() -> Unit) {
        SkyHanniMod.coroutineScope.launch {
            handler(Http.getResult<HypixelMayorData>(
                url = "https://api.hypixel.net/v2/resources/skyblock/election",
                gson = gson,
                errorFactory = ::HypixelError
            ))
        }
    }
}
