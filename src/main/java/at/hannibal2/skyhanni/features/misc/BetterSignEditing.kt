package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.HypixelAPI
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.mixins.transformers.AccessorGuiEditSign
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.mc.McScreen
import at.hannibal2.skyhanni.utils.mc.McScreen.addTextIntoSign
import at.hannibal2.skyhanni.utils.mc.McScreen.setTextIntoSign
import at.hannibal2.skyhanni.utils.system.OS
import kotlinx.coroutines.launch
import net.minecraft.client.gui.GuiScreen

@SkyHanniModule
object BetterSignEditing {

    private var pasteLastClicked = false
    private var copyLastClicked = false
    private var deleteLastClicked = false

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!HypixelAPI.onHypixel) return
        if (!SkyHanniMod.feature.misc.betterSignEditing) return

        checkPaste()
        checkCopying(McScreen.screen)
        checkDeleting(McScreen.screen)
    }

    private fun checkDeleting(gui: GuiScreen?) {
        val deleteClicked = KeyboardManager.isDeleteWordDown() || KeyboardManager.isDeleteLineDown()
        if (!deleteLastClicked && deleteClicked && gui is AccessorGuiEditSign) {
            SkyHanniMod.coroutineScope.launch {
                val newLine = if (KeyboardManager.isDeleteLineDown()) ""
                else if (KeyboardManager.isDeleteWordDown()) {
                    val currentLine = gui.tileSign.signText[gui.editLine].unformattedText

                    val lastSpaceIndex = currentLine.trimEnd().lastIndexOf(' ')
                    if (lastSpaceIndex >= 0) currentLine.substring(0, lastSpaceIndex + 2) else ""
                } else return@launch
                gui.setTextIntoSign(newLine, gui.editLine)
            }
        }
        deleteLastClicked = deleteClicked
    }

    private fun checkCopying(gui: GuiScreen?) {
        val copyClicked = KeyboardManager.isCopyingKeysDown()
        if (!copyLastClicked && copyClicked && gui is AccessorGuiEditSign) {
            SkyHanniMod.coroutineScope.launch {
                OS.copyToClipboard(gui.tileSign.signText[gui.editLine].unformattedText)
            }
        }
        copyLastClicked = copyClicked
    }

    private fun checkPaste() {
        val pasteClicked = KeyboardManager.isPastingKeysDown()
        if (!pasteLastClicked && pasteClicked) {
            McScreen.screen?.addTextIntoSign(OS.readFromClipboard())
        }
        pasteLastClicked = pasteClicked
    }
}
