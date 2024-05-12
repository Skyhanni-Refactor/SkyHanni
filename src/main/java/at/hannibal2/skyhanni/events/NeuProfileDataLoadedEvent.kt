package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.jsonobjects.other.HypixelPlayerApiJson
import at.hannibal2.skyhanni.utils.StringUtils.toDashlessUUID
import at.hannibal2.skyhanni.utils.mc.McPlayer

class NeuProfileDataLoadedEvent(private val playerData: HypixelPlayerApiJson) : LorenzEvent() {
    fun getCurrentProfileData() =
        playerData.profiles.firstOrNull { it.profileName.lowercase() == HypixelData.profileName }

    fun getCurrentPlayerData() = getCurrentProfileData()?.members?.get(McPlayer.uuid.toDashlessUUID())
}
