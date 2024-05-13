package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.mc.McPlayer
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import kotlin.time.Duration.Companion.seconds

object InventoryUtils {

    var itemInHandId = SkyhanniItems.NONE()
    var recentItemsInHand = mutableMapOf<Long, NEUInternalName>()
    var latestItemInHand: ItemStack? = null

    fun getItemsInOpenChest() = buildList<Slot> {
        val guiChest = Minecraft.getMinecraft().currentScreen as? GuiChest ?: return emptyList<Slot>()
        for (slot in guiChest.inventorySlots.inventorySlots) {
            if (slot.inventory is InventoryPlayer) break
            if (slot.stack != null) add(slot)
        }
    }

    // TODO add cache that persists until the next gui/window open/close packet is sent/received
    fun openInventoryName() = Minecraft.getMinecraft().currentScreen.let {
        if (it is GuiChest) {
            val chest = it.inventorySlots as ContainerChest
            chest.getInventoryName()
        } else ""
    }

    fun ContainerChest.getInventoryName() = this.lowerChestInventory.displayName.unformattedText.trim()

    fun inStorage() = openInventoryName().let {
        (it.contains("Storage") && !it.contains("Rift Storage"))
            || it.contains("Ender Chest") || it.contains("Backpack")
    }

    val isNeuStorageEnabled = RecalculatingValue(10.seconds) {
        try {
            val config = NotEnoughUpdates.INSTANCE.config

            val storageField = config.javaClass.getDeclaredField("storageGUI")
            val storage = storageField.get(config)

            val booleanField = storage.javaClass.getDeclaredField("enableStorageGUI3")
            booleanField.get(storage) as Boolean
        } catch (e: Throwable) {
            ErrorManager.logErrorWithData(e, "Could not read NEU config to determine if the neu storage is enabled.")
            false
        }
    }

    fun isSlotInPlayerInventory(itemStack: ItemStack): Boolean {
        val screen = Minecraft.getMinecraft().currentScreen as? GuiContainer ?: return false
        val slotUnderMouse = screen.slotUnderMouse ?: return false
        return slotUnderMouse.inventory is InventoryPlayer && slotUnderMouse.stack == itemStack
    }

    fun ContainerChest.getUpperItems(): Map<Slot, ItemStack> = buildMap {
        for ((slot, stack) in getAllItems()) {
            if (slot.slotNumber != slot.slotIndex) continue
            this[slot] = stack
        }
    }

    fun ContainerChest.getLowerItems(): Map<Slot, ItemStack> = buildMap {
        for ((slot, stack) in getAllItems()) {
            if (slot.slotNumber == slot.slotIndex) continue
            this[slot] = stack
        }
    }

    fun ContainerChest.getAllItems(): Map<Slot, ItemStack> = buildMap {
        for (slot in inventorySlots) {
            if (slot == null) continue
            val stack = slot.stack ?: continue
            this[slot] = stack
        }
    }

    fun getItemAtSlotIndex(slotIndex: Int): ItemStack? {
        return getItemsInOpenChest().find { it.slotIndex == slotIndex }?.stack
    }

    // TODO add a method that also can include the amounts in your sacks
    fun NEUInternalName.getAmountInInventory(): Int = McPlayer.countItems { it.getInternalNameOrNull() == this }
}
