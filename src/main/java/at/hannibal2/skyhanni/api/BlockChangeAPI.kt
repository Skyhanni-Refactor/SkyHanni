package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.events.PacketEvent
import at.hannibal2.skyhanni.events.minecraft.ServerBlockChangeEvent
import net.minecraft.network.play.server.S22PacketMultiBlockChange
import net.minecraft.network.play.server.S23PacketBlockChange
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object BlockChangeAPI {

    @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
    fun onPacketReceive(event: PacketEvent.ReceiveEvent) {
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
