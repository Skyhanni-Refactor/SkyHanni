package at.hannibal2.skyhanni.features.chat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.GuiEditManager
import at.hannibal2.skyhanni.features.garden.fortuneguide.FFGuideGUI
import at.hannibal2.skyhanni.features.misc.visualwords.VisualWordGui
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.mc.McPlayer
import at.hannibal2.skyhanni.utils.mc.McScreen
import io.github.notenoughupdates.moulconfig.gui.GuiScreenElementWrapper
import org.lwjgl.input.Keyboard

object ChatPeek {

    @JvmStatic
    fun peek(): Boolean {
        val key = SkyHanniMod.feature.chat.peekChat

        if (key <= Keyboard.KEY_NONE) return false
        if (!McPlayer.hasPlayer) return false
        if (McScreen.isSignOpen) return false
        if (McScreen.screen is GuiScreenElementWrapper) return false

        if (NEUItems.neuHasFocus()) return false
        if (GuiEditManager.isInGui() || FFGuideGUI.isInGui() || VisualWordGui.isInGui()) return false

        return key.isKeyHeld()
    }
}
