package at.hannibal2.skyhanni.mixins.kotlin

import at.hannibal2.skyhanni.events.minecraft.packet.ReceivePacketEvent
import at.hannibal2.skyhanni.kmixin.annotations.InjectionKind
import at.hannibal2.skyhanni.kmixin.annotations.KInject
import at.hannibal2.skyhanni.kmixin.annotations.KMixin
import io.netty.channel.ChannelHandlerContext
import net.minecraft.network.NetworkManager
import net.minecraft.network.Packet
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@KMixin(NetworkManager::class, priority = 1001)
object NetworkManagerMixin {

    @KInject(method = "channelRead0*", kind = InjectionKind.HEAD, cancellable = true)
    fun onReceivePacket(context: ChannelHandlerContext?, packet: Packet<*>?, ci: CallbackInfo) {
        if (packet != null && ReceivePacketEvent(packet).post()) {
            ci.cancel()
        }
    }
}
