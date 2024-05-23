package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.render.gui.GuiRenderItemEvent
import at.hannibal2.skyhanni.utils.RenderUtils.drawSlotText
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getPetItem
import net.minecraft.client.Minecraft

object PetItemDisplay {

    private val config get() = SkyHanniMod.feature.misc.pets

    @HandleEvent
    fun onRenderItemOverlayPost(event: GuiRenderItemEvent.RenderOverlayEvent.GuiRenderItemPost) {
        val stack = event.stack ?: return
        if (!SkyBlockAPI.isConnected || stack.stackSize != 1) return
        if (config.petItemDisplay.isEmpty()) return

        val petItem = stack.getPetItem() ?: return
        val icon = config.petItemDisplay.firstOrNull { it.item == petItem }?.icon ?: return

        val width = (Minecraft.getMinecraft().fontRendererObj.getStringWidth(icon) * config.petItemDisplayScale).toInt()
        val x = event.x + 22 - width
        val y = event.y - 1

        event.drawSlotText(x, y, icon, config.petItemDisplayScale)
    }
}
