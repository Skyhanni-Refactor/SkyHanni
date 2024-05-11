package at.hannibal2.skyhanni.test.command

import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.mc.McSound

object SoundCommand {

    fun onCommand(args: Array<String>) {
        if (args.isEmpty()) {
            ChatUtils.userError("/shplaysound <soundName> [pitch] [volume]")
            return
        }

        val soundName = args[0]
        val pitch = args.getOrNull(1)?.toFloat() ?: 1.0f
        val volume = args.getOrNull(2)?.toFloat() ?: 50.0f

        McSound.play(soundName, pitch, volume)
    }
}
