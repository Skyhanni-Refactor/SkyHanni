package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.config.core.config.gui.GuiPositionEditor
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.minecraft.KeyPressEvent
import at.hannibal2.skyhanni.events.render.gui.GuiPositionMovedEvent
import at.hannibal2.skyhanni.events.render.gui.GuiRenderEvent
import at.hannibal2.skyhanni.features.garden.GardenOptimalSpeed
import at.hannibal2.skyhanni.test.SkyHanniDebugsAndTests
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.ReflectionUtils.getPropertiesWithType
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import io.github.moulberry.notenoughupdates.itemeditor.GuiElementTextField
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object GuiEditManager {

    private var lastHotkeyPressed = SimpleTimeMark.farPast()

    private var currentPositions = TimeLimitedCache<String, Position>(15.seconds)
    private var currentBorderSize = mutableMapOf<String, Pair<Int, Int>>()
    private var lastMovedGui: String? = null

    @HandleEvent
    fun onKeyClick(event: KeyPressEvent) {
        if (event.keyCode != SkyHanniMod.feature.gui.keyBindOpen) return
        if (isInGui()) return

        Minecraft.getMinecraft().currentScreen?.let {
            if (it !is GuiInventory && it !is GuiChest && it !is GuiEditSign && !(it is GuiProfileViewer && !it.anyTextBoxFocused())) return
            if (it is GuiEditSign && !GardenOptimalSpeed.isRancherSign(it)) return
        }

        if (lastHotkeyPressed.passedSince() < 500.milliseconds) return
        if (NEUItems.neuHasFocus()) return
        lastHotkeyPressed = SimpleTimeMark.now()

        openGuiPositionEditor(hotkeyReminder = false)
    }

    @HandleEvent(HandleEvent.LOWEST)
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
    }

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        lastMovedGui?.let {
            GuiPositionMovedEvent(it).post()
            lastMovedGui = null
        }
    }

    @JvmStatic
    fun add(position: Position, posLabel: String, x: Int, y: Int) {
        var name = position.internalName
        if (name == null) {
            name = if (posLabel == "none") "none " + UUID.randomUUID() else posLabel
            position.internalName = name
        }
        currentPositions.put(name, position)
        currentBorderSize[posLabel] = Pair(x, y)
    }

    private var lastHotkeyReminded = SimpleTimeMark.farPast()

    @JvmStatic
    fun openGuiPositionEditor(hotkeyReminder: Boolean) {
        SkyHanniMod.screenToOpen = GuiPositionEditor(
            currentPositions.values().toList(),
            2,
            Minecraft.getMinecraft().currentScreen as? GuiContainer
        )
        if (hotkeyReminder && lastHotkeyReminded.passedSince() > 30.minutes) {
            lastHotkeyReminded = SimpleTimeMark.now()
            ChatUtils.chat(
                "§eTo edit hidden GUI elements:\n" +
                    " §7- §e1. Set a key in /sh edit.\n" +
                    " §7- §e2. Click that key while the GUI element is visible."
            )
        }
    }

    @JvmStatic
    fun renderLast() {
        if (!isInGui()) return
        if (!SkyHanniDebugsAndTests.globalRender) return

        GlStateManager.translate(0f, 0f, 200f)

        GuiRenderEvent.GuiOverlayRenderEvent().post()

        GlStateManager.pushMatrix()
        GlStateManager.enableDepth()
        GuiRenderEvent.ChestGuiOverlayRenderEvent().post()
        GlStateManager.popMatrix()

        GlStateManager.translate(0f, 0f, -200f)
    }

    fun isInGui() = Minecraft.getMinecraft().currentScreen is GuiPositionEditor

    fun Position.getDummySize(random: Boolean = false): Vector2i {
        if (random) return Vector2i(5, 5)
        val (x, y) = currentBorderSize[internalName] ?: return Vector2i(1, 1)
        return Vector2i((x * effectiveScale).toInt(), (y * effectiveScale).toInt())
    }

    fun Position.getAbsX() = getAbsX0(getDummySize(true).x)

    fun Position.getAbsY() = getAbsY0(getDummySize(true).y)

    fun GuiProfileViewer.anyTextBoxFocused() =
        this.getPropertiesWithType<GuiElementTextField>().any { it.focus }

    fun handleGuiPositionMoved(guiName: String) {
        lastMovedGui = guiName
    }
}

class Vector2i(val x: Int, val y: Int)
