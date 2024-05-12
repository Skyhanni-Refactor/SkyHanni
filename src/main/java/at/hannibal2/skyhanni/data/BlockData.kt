package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.events.PacketEvent
import at.hannibal2.skyhanni.events.ServerBlockChangeEvent
import net.minecraft.network.play.server.S22PacketMultiBlockChange
import net.minecraft.network.play.server.S23PacketBlockChange
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class BlockData {

    @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
    fun onChatPacket(event: PacketEvent.ReceiveEvent) {
        if (event.packet is S23PacketBlockChange) {
            val blockPos = event.packet.blockPosition ?: return
            val blockState = event.packet.blockState ?: return
            ServerBlockChangeEvent(blockPos, blockState).postAndCatch()
        } else if (event.packet is S22PacketMultiBlockChange) {
            for (block in event.packet.changedBlocks) {
                ServerBlockChangeEvent(block.pos, block.blockState).postAndCatch()
            }
        }
    }
}
