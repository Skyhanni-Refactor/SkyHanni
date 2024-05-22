package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.events.inventory.ItemInHandChangeEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.minecraft.PlaySoundEvent
import at.hannibal2.skyhanni.events.minecraft.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.minecraft.packet.ReceivePacketEvent
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.mc.McPlayer
import net.minecraft.client.Minecraft
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object MinecraftData {

    @HandleEvent(receiveCancelled = true)
    fun onSoundPacket(event: ReceivePacketEvent) {
        if (!LorenzUtils.inSkyBlock) return

        val packet = event.packet
        if (packet !is S29PacketSoundEffect) return

        if (PlaySoundEvent(
                packet.soundName,
                LorenzVec(packet.x, packet.y, packet.z),
                packet.pitch,
                packet.volume
            ).post()
        ) {
            event.cancel()
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        WorldChangeEvent().post()
    }

    @HandleEvent(receiveCancelled = true)
    fun onParticlePacketReceive(event: ReceivePacketEvent) {
        if (!LorenzUtils.inSkyBlock) return

        val packet = event.packet
        if (packet !is S2APacketParticles) return

        if (ReceiveParticleEvent(
                packet.particleType!!,
                LorenzVec(packet.xCoordinate, packet.yCoordinate, packet.zCoordinate),
                packet.particleCount,
                packet.particleSpeed,
                LorenzVec(packet.xOffset, packet.yOffset, packet.zOffset),
                packet.isLongDistance,
                packet.particleArgs,
            ).post()
        ) {
            event.cancel()
        }
    }

    var totalTicks = 0

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.START) return
        Minecraft.getMinecraft().thePlayer ?: return

        DelayedRun.checkRuns()
        totalTicks++
        ClientTickEvent(totalTicks).post()
    }

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!LorenzUtils.inSkyBlock) return
        val hand = McPlayer.heldItem
        val newItem = hand?.getInternalName() ?: SkyhanniItems.NONE()
        val oldItem = InventoryUtils.itemInHandId
        if (newItem != oldItem) {

            InventoryUtils.recentItemsInHand.keys.removeIf { it + 30_000 > System.currentTimeMillis() }
            if (newItem != SkyhanniItems.NONE()) {
                InventoryUtils.recentItemsInHand[System.currentTimeMillis()] = newItem
            }
            InventoryUtils.itemInHandId = newItem
            InventoryUtils.latestItemInHand = hand
            ItemInHandChangeEvent(newItem, oldItem).post()
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        InventoryUtils.itemInHandId = SkyhanniItems.NONE()
        InventoryUtils.recentItemsInHand.clear()
    }
}
