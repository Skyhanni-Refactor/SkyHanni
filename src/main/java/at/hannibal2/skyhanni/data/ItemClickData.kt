package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.events.BlockClickEvent
import at.hannibal2.skyhanni.events.EntityClickEvent
import at.hannibal2.skyhanni.events.ItemClickEvent
import at.hannibal2.skyhanni.events.PacketEvent
import at.hannibal2.skyhanni.utils.mc.McPlayer
import at.hannibal2.skyhanni.utils.mc.McWorld
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ItemClickData {

    @SubscribeEvent
    fun onItemClickSend(event: PacketEvent.SendEvent) {
        val packet = event.packet
        event.isCanceled = when {
            packet is C08PacketPlayerBlockPlacement -> {
                if (packet.placedBlockDirection != 255) {
                    val position = packet.position.toLorenzVec()
                    BlockClickEvent(ClickType.RIGHT_CLICK, position, packet.stack).postAndCatch()
                } else {
                    ItemClickEvent(McPlayer.heldItem, ClickType.RIGHT_CLICK).postAndCatch()
                }
            }

            packet is C07PacketPlayerDigging && packet.status == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK -> {
                val position = packet.position.toLorenzVec()
                val blockClickCancelled =
                    BlockClickEvent(ClickType.LEFT_CLICK, position, McPlayer.heldItem).postAndCatch()
                ItemClickEvent(McPlayer.heldItem, ClickType.LEFT_CLICK).also {
                    it.isCanceled = blockClickCancelled
                }.postAndCatch()
            }

            packet is C0APacketAnimation -> {
                ItemClickEvent(McPlayer.heldItem, ClickType.LEFT_CLICK).postAndCatch()
            }

            packet is C02PacketUseEntity -> {
                val clickType = when (packet.action) {
                    C02PacketUseEntity.Action.INTERACT -> ClickType.RIGHT_CLICK
                    C02PacketUseEntity.Action.ATTACK -> ClickType.LEFT_CLICK
                    C02PacketUseEntity.Action.INTERACT_AT -> ClickType.RIGHT_CLICK
                    else -> return
                }
                val clickedEntity = packet.getEntityFromWorld(McWorld.world) ?: return
                EntityClickEvent(clickType, clickedEntity, McPlayer.heldItem).postAndCatch()
            }

            else -> {
                return
            }
        }
    }
}
