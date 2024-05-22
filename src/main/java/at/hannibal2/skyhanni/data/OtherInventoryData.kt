package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.inventory.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.inventory.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.minecraft.packet.ReceivePacketEvent
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2EPacketCloseWindow
import net.minecraft.network.play.server.S2FPacketSetSlot

object OtherInventoryData {

    private var currentInventory: Inventory? = null
    private var acceptItems = false
    private var lateEvent: InventoryUpdatedEvent? = null

    fun close(reopenSameName: Boolean = false) {
        InventoryCloseEvent(reopenSameName).post()
        currentInventory = null
    }

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        lateEvent?.let {
            it.post()
            lateEvent = null
        }
    }

    @HandleEvent
    fun onInventoryDataReceiveEvent(event: ReceivePacketEvent) {
        when (val packet = event.packet) {
            is S2EPacketCloseWindow -> close()
            is S2DPacketOpenWindow -> {
                val windowId = packet.windowId
                val title = packet.windowTitle.unformattedText
                val slotCount = packet.slotCount
                close(reopenSameName = title == currentInventory?.title)
                currentInventory = Inventory(windowId, title, slotCount)
                acceptItems = true
            }

            is S2FPacketSetSlot -> {
                if (!acceptItems) {
                    currentInventory?.let {
                        if (it.windowId != packet.func_149175_c()) return
                        val slot = packet.func_149173_d()
                        if (slot < it.slotCount) {
                            val itemStack = packet.func_149174_e()
                            if (itemStack != null) {
                                it.items[slot] = itemStack
                                lateEvent = InventoryUpdatedEvent(it)
                            }
                        }
                    }
                    return
                }
                currentInventory?.let {
                    if (it.windowId != packet.func_149175_c()) return
                    val slot = packet.func_149173_d()
                    if (slot < it.slotCount) {
                        val itemStack = packet.func_149174_e()
                        if (itemStack != null) {
                            it.items[slot] = itemStack
                        }
                    } else {
                        done(it)
                        return
                    }
                    if (it.items.size == it.slotCount) {
                        done(it)
                    }
                }
            }
        }
    }

    private fun done(inventory: Inventory) {
        InventoryFullyOpenedEvent(inventory).post()
        inventory.fullyOpenedOnce = true
        InventoryUpdatedEvent(inventory).post()
        acceptItems = false
    }

    class Inventory(
        val windowId: Int,
        val title: String,
        val slotCount: Int,
        val items: MutableMap<Int, ItemStack> = mutableMapOf(),
        var fullyOpenedOnce: Boolean = false,
    )
}
