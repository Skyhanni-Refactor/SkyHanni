package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.events.minecraft.ClientDisconnectEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent


/**
 * Handles all posting of FML events to SkyHanni events.
 */
object FmlEventAPI {

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
       ClientDisconnectEvent().post()
    }
}
