package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.HypixelAPI
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.inventory.ItemAddInInventoryEvent
import at.hannibal2.skyhanni.events.inventory.OwnInventoryItemUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.minecraft.packet.ReceivePacketEvent
import at.hannibal2.skyhanni.events.minecraft.packet.SendPacketEvent
import at.hannibal2.skyhanni.events.render.gui.SlotClickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.mc.McPlayer
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S0DPacketCollectItem
import net.minecraft.network.play.server.S2FPacketSetSlot
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object OwnInventoryData {

    private var itemAmounts = mapOf<NEUInternalName, Int>()
    private var dirty = false
    private val sackToInventoryChatPattern by RepoPattern.pattern(
        "data.owninventory.chat.movedsacktoinventory",
        "§aMoved §r§e\\d* (?<name>.*)§r§a from your Sacks to your inventory."
    )

    @HandleEvent(onlyOnSkyblock = true, priority = HandleEvent.LOW, receiveCancelled = true)
    fun onItemPickupReceivePacket(event: ReceivePacketEvent) {
        val packet = event.packet
        if (packet is S2FPacketSetSlot || packet is S0DPacketCollectItem) {
            dirty = true
        }
        if (packet is S2FPacketSetSlot) {
            val windowId = packet.func_149175_c()
            if (windowId == 0) {
                val slot = packet.func_149173_d()
                val item = packet.func_149174_e() ?: return
                DelayedRun.runNextTick {
                    OwnInventoryItemUpdateEvent(item, slot).post()
                }
            }
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onClickEntity(event: SendPacketEvent) {
        val packet = event.packet

        if (packet is C0EPacketClickWindow) {
            dirty = true
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onTick(event: ClientTickEvent) {
        if (itemAmounts.isEmpty()) {
            itemAmounts = getCurrentItems()
        }

        if (!dirty) return
        dirty = false

        val map = getCurrentItems()
        for ((internalName, amount) in map) {
            calculateDifference(internalName, amount)
        }
        itemAmounts = map
    }

    private fun getCurrentItems(): MutableMap<NEUInternalName, Int> {
        val map = mutableMapOf<NEUInternalName, Int>()
        for (itemStack in McPlayer.inventory) {
            val internalName = itemStack.getInternalNameOrNull() ?: continue
            map.addOrPut(internalName, itemStack.stackSize)
        }
        return map
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        itemAmounts = emptyMap()
    }

    private fun calculateDifference(internalName: NEUInternalName, newAmount: Int) {
        val oldAmount = itemAmounts[internalName] ?: 0

        val diff = newAmount - oldAmount
        if (diff > 0) {
            addItem(internalName, diff)
        }
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        val item = Minecraft.getMinecraft().thePlayer.inventory.itemStack ?: return
        val internalNameOrNull = item.getInternalNameOrNull() ?: return
        ignoreItem(500.milliseconds) { it == internalNameOrNull }
    }

    @HandleEvent
    fun onSlotClick(event: SlotClickEvent) {
        ignoreItem(500.milliseconds) { true }
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        sackToInventoryChatPattern.matchMatcher(event.message) {
            val name = group("name")
            ignoreItem(500.milliseconds) { it.itemName.contains(name) }
        }
    }

    private fun ignoreItem(duration: Duration, condition: (NEUInternalName) -> Boolean) {
        ignoredItemsUntil.add(IgnoredItem(condition, SimpleTimeMark.now() + duration))
    }

    private val ignoredItemsUntil = mutableListOf<IgnoredItem>()

    class IgnoredItem(val condition: (NEUInternalName) -> Boolean, val blockedUntil: SimpleTimeMark)

    private fun addItem(internalName: NEUInternalName, add: Int) {
        if (HypixelAPI.lastWorldChange.passedSince() < 3.seconds) return

        ignoredItemsUntil.removeIf { it.blockedUntil.isInPast() }
        if (ignoredItemsUntil.any { it.condition(internalName) }) {
            return
        }

        if (internalName.startsWith("MAP-")) return

        ItemAddInInventoryEvent(internalName, add).post()
    }
}
