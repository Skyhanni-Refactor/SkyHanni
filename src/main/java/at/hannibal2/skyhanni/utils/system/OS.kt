package at.hannibal2.skyhanni.utils.system

import at.hannibal2.skyhanni.test.command.ErrorManager
import net.minecraft.client.gui.GuiScreen
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.util.concurrent.TimeUnit

object OS {

    private val LINUX_OPEN_COMMANDS = arrayOf("xdg-open", "gnome-open", "kde-open")

    val isWindows: Boolean
    val isMac: Boolean
    val isLinux: Boolean

    init {
        val os = System.getProperty("os.name").lowercase()
        isWindows = os.contains("win")
        isMac = os.contains("mac")
        isLinux = os.contains("linux")
    }

    @JvmStatic
    fun copyToClipboard(text: String) {
        GuiScreen.setClipboardString(text)
    }

    @JvmStatic
    fun readFromClipboard(): String {
        return GuiScreen.getClipboardString()
    }

    @JvmStatic
    fun openUrl(url: String) {
        if (!tryDesktop { it.browse(URI(url)) } && !openWithExec(url)) {
            copyToClipboard(url)
            ErrorManager.logErrorStateWithData(
                "Cannot open website! Copied url to clipboard instead",
                "Web browser is not supported",
                "url" to url,
            )
        }
    }

    @JvmStatic
    fun openFile(file: String) {
        if (!tryDesktop { it.open(File(file)) } && !openWithExec(file)) {
            copyToClipboard(file)
            ErrorManager.logErrorStateWithData(
                "Cannot open file! Copied file path to clipboard instead",
                "File opener is not supported",
                "file" to file,
            )
        }
    }

    private fun tryDesktop(action: (Desktop) -> Unit): Boolean {
        if (Desktop.isDesktopSupported()) {
            val desktop = Desktop.getDesktop()
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    action(desktop)
                    return true
                } catch (_: Exception) {
                }
            }
        }
        return false
    }

    private fun openWithExec(url: String): Boolean {
        return when {
            isWindows -> execCommand("rundll32", "url.dll,FileProtocolHandler", url)
            isMac -> execCommand("open", url)
            isLinux -> LINUX_OPEN_COMMANDS.any { execCommand(it, url) }
            else -> false
        }
    }

    private fun execCommand(vararg args: String, waitForProcess: Boolean = false): Boolean {
        try {
            val process = ProcessBuilder(*args).start() ?: return false
            if (waitForProcess) {
                return !process.waitFor(2, TimeUnit.SECONDS) || process.exitValue() == 0
            }
            return process.isAlive
        } catch (_: Exception) {
        }
        return false
    }


}
