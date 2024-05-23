package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.render.gui.GuiKeyPressEvent
import at.hannibal2.skyhanni.events.render.gui.SlotClickEvent
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyClicked
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import kotlin.time.Duration.Companion.milliseconds

object ChocolateFactoryKeybinds {
    private val config get() = ChocolateFactoryAPI.config.keybinds
    private var lastClick = SimpleTimeMark.farPast()

    @HandleEvent
    fun onKeyPress(event: GuiKeyPressEvent) {
        if (!SkyBlockAPI.isConnected) return
        if (!config.enabled) return
        if (!ChocolateFactoryAPI.inChocolateFactory) return

        val chest = event.guiContainer as? GuiChest ?: return

        for (index in 0..4) {
            val key = getKey(index) ?: error("no key for index $index")
            if (!key.isKeyClicked()) continue
            if (lastClick.passedSince() < 200.milliseconds) break
            lastClick = SimpleTimeMark.now()

            event.cancel()

            Minecraft.getMinecraft().playerController.windowClick(
                chest.inventorySlots.windowId,
                29 + index,
                2,
                3,
                Minecraft.getMinecraft().thePlayer
            )
            break
        }
    }

    @HandleEvent
    fun onSlotClick(event: SlotClickEvent) {
        if (!SkyBlockAPI.isConnected) return
        if (!config.enabled) return
        if (!ChocolateFactoryAPI.inChocolateFactory) return

        // needed to not send duplicate clicks via keybind feature
        if (event.clickTypeEnum == SlotClickEvent.ClickType.HOTBAR) {
            event.cancel()
        }
    }

    private fun getKey(index: Int) = when (index) {
        0 -> config.key1
        1 -> config.key2
        2 -> config.key3
        3 -> config.key4
        4 -> config.key5
        else -> null
    }
}
