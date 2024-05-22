package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigGuiManager
import at.hannibal2.skyhanni.test.command.ErrorManager
import io.github.notenoughupdates.moulconfig.gui.GuiScreenElementWrapper
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor
import io.github.notenoughupdates.moulconfig.processor.ProcessedOption
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.jvm.javaField

object ConfigUtils {

    private fun KMutableProperty0<*>.tryFindEditor(editor: MoulConfigEditor<*>): ProcessedOption? {
        return editor.processedConfig.getOptionFromField(this.javaField ?: return null)
    }

    fun KMutableProperty0<*>.jumpToEditor() {
        if (tryJumpToEditor(ConfigGuiManager.getEditorInstance())) return

        ErrorManager.logErrorStateWithData(
            "Can not open the config",
            "error while trying to jump to an editor element",
            "this.name" to this.name,
            "this.toString()" to this.toString(),
            "this" to this,
        )
    }

    private fun KMutableProperty0<*>.tryJumpToEditor(editor: MoulConfigEditor<*>): Boolean {
        val option = tryFindEditor(editor) ?: return false
        editor.search("")
        if (!editor.goToOption(option)) return false
        SkyHanniMod.screenToOpen = GuiScreenElementWrapper(editor)
        return true
    }
}
