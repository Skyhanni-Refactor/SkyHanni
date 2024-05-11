package at.hannibal2.skyhanni.test.command

import at.hannibal2.skyhanni.data.ActionBarData
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.system.OS

object CopyActionBarCommand {
    fun command(args: Array<String>) {
        val noFormattingCodes = args.size == 1 && args[0] == "true"

        val status = if (noFormattingCodes) "without" else "with"

        var actionBar = ActionBarData.getActionBar()
        if (noFormattingCodes) actionBar = actionBar.removeColor()

        OS.copyToClipboard(actionBar)
        ChatUtils.chat("Action bar name copied to clipboard $status formatting codes!")
    }
}
