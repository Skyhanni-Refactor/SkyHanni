package at.hannibal2.skyhanni.events.render.gui

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Container

data class BackgroundDrawnEvent(
    val gui: GuiContainer,
    val container: Container,
    val mouseX: Int,
    val mouseY: Int,
    val partialTicks: Float,
) : SkyHanniEvent()
