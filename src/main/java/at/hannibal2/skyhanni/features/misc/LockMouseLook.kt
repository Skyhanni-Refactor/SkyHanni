package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.render.gui.GuiOverlayRenderEvent
import at.hannibal2.skyhanni.events.utils.DebugDataCollectEvent
import at.hannibal2.skyhanni.features.garden.SensitivityReducer
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.mc.McClient

object LockMouseLook {

    private val config get() = SkyHanniMod.feature.misc
    private val storage get() = SkyHanniMod.feature.storage
    var lockedMouse = false
    private const val LOCKED_POSITION = -1F / 3F

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        if (lockedMouse) toggleLock()
        if (McClient.options.mouseSensitivity == LOCKED_POSITION) {
            McClient.options.mouseSensitivity = storage.savedMouselockedSensitivity
            ChatUtils.chat("§bMouse rotation is now unlocked because you left it locked.")
        }
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!event.message.startsWith("§aTeleported you to §r§aPlot")) return
        if (lockedMouse) toggleLock()
    }

    fun toggleLock() {
        lockedMouse = !lockedMouse

        var mouseSensitivity = McClient.options.mouseSensitivity
        if (SensitivityReducer.isEnabled()) mouseSensitivity = SensitivityReducer.doTheMath(mouseSensitivity, true)

        if (lockedMouse) {
            storage.savedMouselockedSensitivity = mouseSensitivity
            McClient.options.mouseSensitivity = LOCKED_POSITION
            if (config.lockMouseLookChatMessage) {
                ChatUtils.chat("§bMouse rotation is now locked. Type /shmouselock to unlock your rotation")
            }
        } else {
            if (!SensitivityReducer.isEnabled()) McClient.options.mouseSensitivity = storage.savedMouselockedSensitivity
            else McClient.options.mouseSensitivity = SensitivityReducer.doTheMath(storage.savedMouselockedSensitivity)
            if (config.lockMouseLookChatMessage) {
                ChatUtils.chat("§bMouse rotation is now unlocked.")
            }
        }
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiOverlayRenderEvent) {
        if (!lockedMouse) return
        config.lockedMouseDisplay.renderString("§eMouse Locked", posLabel = "Mouse Locked")
    }

    fun autoDisable() {
        if (lockedMouse) {
            toggleLock()
        }
    }

    @HandleEvent
    fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Mouse Lock")

        if (!lockedMouse) {
            event.addIrrelevant("not enabled")
            return
        }

        event.addData {
            add("Stored Sensitivity: ${storage.savedMouselockedSensitivity}")
        }
    }
}
