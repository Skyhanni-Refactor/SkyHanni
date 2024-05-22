package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.ServerBlockChangeEvent
import at.hannibal2.skyhanni.events.minecraft.packet.ReceivePacketEvent
import net.minecraft.network.play.server.S22PacketMultiBlockChange
import net.minecraft.network.play.server.S23PacketBlockChange

object BlockChangeAPI {

    @HandleEvent(priority = HandleEvent.LOW, receiveCancelled = true)
    fun onPacketReceive(event: ReceivePacketEvent) {
        if (event.packet is S23PacketBlockChange) {
            val blockPos = event.packet.blockPosition ?: return
            val blockState = event.packet.blockState ?: return
            ServerBlockChangeEvent(blockPos, blockState).post()
        } else if (event.packet is S22PacketMultiBlockChange) {
            for (block in event.packet.changedBlocks) {
                ServerBlockChangeEvent(block.pos, block.blockState).post()
            }
        }
    }
}
