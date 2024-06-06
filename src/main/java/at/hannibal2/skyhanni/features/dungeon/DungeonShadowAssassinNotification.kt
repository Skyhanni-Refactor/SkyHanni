package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.minecraft.packet.ReceivePacketEvent
import at.hannibal2.skyhanni.mixins.transformers.AccessorWorldBoarderPacket
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.mc.McSound
import at.hannibal2.skyhanni.utils.mc.McSound.play
import net.minecraft.network.play.server.S44PacketWorldBorder
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object DungeonShadowAssassinNotification {
    private val config get() = SkyHanniMod.feature.dungeon

    @HandleEvent
    fun onWorldBoarderChange(event: ReceivePacketEvent) {
        if (!isEnabled()) return
        if (DungeonAPI.dungeonFloor?.contains("3") == true && DungeonAPI.inBossRoom) return

        val packet = event.packet as? AccessorWorldBoarderPacket ?: return
        val action = packet.action
        val warningTime = packet.warningTime

        if (action == S44PacketWorldBorder.Action.INITIALIZE && warningTime == 10000) {
            TitleManager.sendTitle("§cShadow Assassin Jumping!", 2.seconds, 3.6, 7.0f)
            McSound.BEEP.play()
        }
    }

    private fun isEnabled() = DungeonAPI.inDungeon() && config.shadowAssassinJumpNotifier
}
