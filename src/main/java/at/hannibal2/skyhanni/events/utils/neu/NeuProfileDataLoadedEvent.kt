package at.hannibal2.skyhanni.events.utils.neu

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.jsonobjects.other.HypixelPlayerApiJson
import at.hannibal2.skyhanni.utils.StringUtils.toDashlessUUID
import at.hannibal2.skyhanni.utils.mc.McPlayer

class NeuProfileDataLoadedEvent(private val playerData: HypixelPlayerApiJson) : SkyHanniEvent() {
    fun getCurrentProfileData() =
        playerData.profiles.firstOrNull { it.profileName.lowercase() == HypixelData.profileName }

    fun getCurrentPlayerData() = getCurrentProfileData()?.members?.get(McPlayer.uuid.toDashlessUUID())
}
