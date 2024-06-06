package at.hannibal2.skyhanni.events.minecraft.packet

import at.hannibal2.skyhanni.api.event.CancellableSkyHanniEvent
import net.minecraft.network.Packet

class ReceivePacketEvent(val packet: Packet<*>) : CancellableSkyHanniEvent()
