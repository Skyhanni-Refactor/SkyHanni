package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.PacketEvent
import at.hannibal2.skyhanni.events.hypixel.HypixelLocationEvent
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.mc.McClient
import at.hannibal2.skyhanni.utils.network.ByteBufUtils.readNullable
import at.hannibal2.skyhanni.utils.network.ByteBufUtils.readString
import at.hannibal2.skyhanni.utils.network.ByteBufUtils.readVarInt
import at.hannibal2.skyhanni.utils.network.HexDumper
import io.netty.buffer.Unpooled
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.C17PacketCustomPayload
import net.minecraft.network.play.server.S3FPacketCustomPayload
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import kotlin.time.Duration.Companion.seconds

object HypixelPacketAPI {

    private val events = mutableMapOf<String, Int>()
    var connected: Boolean = false
        private set
    var envrionemnt: Environment = Environment.PROD
        private set

    @SubscribeEvent
    fun onPacketReceive(event: PacketEvent.ReceiveEvent) {
        if (event.packet !is S3FPacketCustomPayload) return
        
        val packet = event.packet
        val channel = packet.channelName
        val data = packet.bufferData

        if (SkyHanniMod.feature.dev.debug.logCustomPackets) {
            SkyHanniMod.logger.info("Received custom packet: $channel\n${HexDumper.dump(data)}")
        }

        when (channel) {
            "hyevent:location" -> {
                data.use {
                    val successful = data.readBoolean()
                    if (!successful) return@use
                    data.readVarInt()
                    val server = data.readString()
                    val type = data.readNullable { data.readString() }
                    val lobby = data.readNullable { data.readString() }
                    val mode = data.readNullable { data.readString() }
                    val map = data.readNullable { data.readString() }

                    HypixelLocationEvent(server, type, lobby, mode, map).post()
                }
            }
            "hypixel:hello" -> {
                DelayedRun.runDelayed(2.5.seconds) {
                    registerLocation(McClient.network)
                }
                connected = true
                data.use {
                    envrionemnt = when (data.readVarInt()) {
                        0 -> Environment.PROD
                        1 -> Environment.ALPHA
                        2 -> Environment.TEST
                        else -> Environment.PROD
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onPacketSend(event: PacketEvent.SendEvent) {
        if (event.packet !is C17PacketCustomPayload) return

        val packet = event.packet
        val channel = packet.channelName
        val data = packet.bufferData

        when (channel) {
            "hypixel:register" -> {
                data.use {
                    events.clear()
                    data.readByte()
                    repeat(data.readVarInt()) {
                        events[data.readString()] = data.readVarInt()
                    }

                    if (events["hyevent:location"] == null) {
                        event.cancel()
                        registerLocation(event.network)
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onServerDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        connected = false
    }

    private fun registerLocation(network: NetHandlerPlayClient) {
        val buffer = PacketBuffer(Unpooled.buffer())
        buffer.writeByte(1)
        val copy = events.editCopy { this["hyevent:location"] = 1 }
        buffer.writeVarIntToBuffer(copy.size)
        copy.forEach { (key, value) ->
            buffer.writeString(key)
            buffer.writeVarIntToBuffer(value)
        }
        network.addToSendQueue(C17PacketCustomPayload("hypixel:register", buffer))
    }

    private fun PacketBuffer.use(block: PacketBuffer.() -> Unit) {
        block()
        resetReaderIndex()
    }

    enum class Environment {
        PROD,
        ALPHA,
        TEST
    }
}
