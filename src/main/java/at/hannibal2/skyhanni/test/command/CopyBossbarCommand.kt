package at.hannibal2.skyhanni.test.command

import at.hannibal2.skyhanni.api.BossbarAPI
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.system.OS

object CopyBossbarCommand {
    fun command(args: Array<String>) {
        val noFormattingCodes = args.size == 1 && args[0] == "true"
        val bossbarName = if (noFormattingCodes) BossbarAPI.getBossbar().removeColor() else BossbarAPI.getBossbar()
        val status = if (noFormattingCodes) "without" else "with"
        if (bossbarName.isBlank()) {
            ChatUtils.chat("Boss bar appears to be blank.")
        } else {
            OS.copyToClipboard(bossbarName)
            ChatUtils.chat("Boss bar name copied to clipboard $status formatting codes!")
        }
    }
}
