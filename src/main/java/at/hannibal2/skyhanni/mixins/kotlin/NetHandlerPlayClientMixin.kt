package at.hannibal2.skyhanni.mixins.kotlin

import at.hannibal2.skyhanni.events.entity.EntityAttributeUpdateEvent
import at.hannibal2.skyhanni.events.entity.EntityEquipmentChangeEvent
import at.hannibal2.skyhanni.events.minecraft.packet.SendPacketEvent
import at.hannibal2.skyhanni.kmixin.annotations.InjectionKind
import at.hannibal2.skyhanni.kmixin.annotations.KInject
import at.hannibal2.skyhanni.kmixin.annotations.KInjectAt
import at.hannibal2.skyhanni.kmixin.annotations.KMixin
import at.hannibal2.skyhanni.kmixin.annotations.KSelf
import at.hannibal2.skyhanni.kmixin.annotations.TargetShift
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.entity.Entity
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S04PacketEntityEquipment
import net.minecraft.network.play.server.S20PacketEntityProperties
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@KMixin(NetHandlerPlayClient::class, priority = 1001)
object NetHandlerPlayClientMixin {

    @KInject(method = "addToSendQueue", kind = InjectionKind.HEAD, cancellable = true)
    fun onSendPacket(packet: Packet<*>, ci: CallbackInfo, @KSelf client: NetHandlerPlayClient) {
        if (SendPacketEvent(client, packet).post()) {
            ci.cancel()
        }
    }

    @KInjectAt(
        method = "handleEntityEquipment",
        target = "Lnet/minecraft/entity/Entity;setCurrentItemOrArmor(ILnet/minecraft/item/ItemStack;)V",
        shift = TargetShift.AFTER,
        captureLocals = true
    )
    fun onEntityEquipment(packetIn: S04PacketEntityEquipment, ci: CallbackInfo, entity: Entity) {
        EntityEquipmentChangeEvent(entity, packetIn.equipmentSlot, packetIn.itemStack).post()
    }

    @KInjectAt(
        method = "handleEntityProperties",
        target = "Lnet/minecraft/entity/EntityLivingBase;getAttributeMap()Lnet/minecraft/entity/ai/attributes/BaseAttributeMap;",
        captureLocals = true
    )
    fun onEntityProperties(packetIn: S20PacketEntityProperties, ci: CallbackInfo, entity: Entity) {
        EntityAttributeUpdateEvent(entity, packetIn.func_149441_d()).post()
    }

}
