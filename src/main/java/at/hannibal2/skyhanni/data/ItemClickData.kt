package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.click.BlockClickEvent
import at.hannibal2.skyhanni.events.minecraft.click.EntityClickEvent
import at.hannibal2.skyhanni.events.minecraft.click.ItemClickEvent
import at.hannibal2.skyhanni.events.minecraft.packet.SendPacketEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.mc.McPlayer
import at.hannibal2.skyhanni.utils.mc.McWorld
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0APacketAnimation

@SkyHanniModule
object ItemClickData {

    @HandleEvent
    fun onItemClickSend(event: SendPacketEvent) {
        val packet = event.packet
        val cancelled = when {
            packet is C08PacketPlayerBlockPlacement -> {
                if (packet.placedBlockDirection != 255) {
                    val position = packet.position.toLorenzVec()
                    BlockClickEvent(ClickType.RIGHT_CLICK, position, packet.stack).post()
                } else {
                    ItemClickEvent(McPlayer.heldItem, ClickType.RIGHT_CLICK).post()
                }
            }

            packet is C07PacketPlayerDigging && packet.status == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK -> {
                val position = packet.position.toLorenzVec()
                val blockClickCancelled =
                    BlockClickEvent(ClickType.LEFT_CLICK, position, McPlayer.heldItem).post()
                ItemClickEvent(McPlayer.heldItem, ClickType.LEFT_CLICK).also {
                    if (blockClickCancelled) it.cancel()
                }.post()
            }

            packet is C0APacketAnimation -> {
                ItemClickEvent(McPlayer.heldItem, ClickType.LEFT_CLICK).post()
            }

            packet is C02PacketUseEntity -> {
                val clickType = when (packet.action) {
                    C02PacketUseEntity.Action.INTERACT -> ClickType.RIGHT_CLICK
                    C02PacketUseEntity.Action.ATTACK -> ClickType.LEFT_CLICK
                    C02PacketUseEntity.Action.INTERACT_AT -> ClickType.RIGHT_CLICK
                    else -> return
                }
                val clickedEntity = packet.getEntityFromWorld(McWorld.world) ?: return
                EntityClickEvent(clickType, clickedEntity, McPlayer.heldItem).post()
            }

            else -> {
                return
            }
        }

        if (cancelled) event.cancel()
    }
}
