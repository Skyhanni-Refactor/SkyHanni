package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.skyblock.BitsUpdateEvent
import at.hannibal2.skyhanni.events.utils.ConfigFixEvent
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.mc.McSound
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

object NoBitsWarning {

    private val config get() = SkyHanniMod.feature.misc.bits

    @HandleEvent
    fun onBitsGain(event: BitsUpdateEvent.BitsGain) {
        if (isWarningEnabled() && event.bitsAvailable == 0) {

            ChatUtils.clickableChat(
                "§bNo Bits Available! §eClick to run /bz booster cookie.",
                onClick = {
                    HypixelCommands.bazaar("booster cookie")
                }
            )
            TitleManager.sendTitle("§bNo Bits Available", 5.seconds)
            if (config.notificationSound) {
                McSound.playOnRepeat("note.pling", 0.6f, 1f, 100, 10)
            }
        }

        if (isChatMessageEnabled()) {
            if (event.bits < config.threshold) return
            ChatUtils.chat("You have gained §b${event.difference.addSeparators()} §eBits.")
        }
    }

    private fun isChatMessageEnabled() = LorenzUtils.inSkyBlock && config.bitsGainChatMessage
    private fun isWarningEnabled() = LorenzUtils.inSkyBlock && config.enableWarning

    @HandleEvent
    fun onConfigFix(event: ConfigFixEvent) {
        event.move(35, "misc.noBitsWarning", "misc.noBitsWarning.enabled")
        event.move(40, "misc.noBitsWarning.enabled", "misc.bits.enableWarning")
        event.move(40, "misc.noBitsWarning.notificationSound", "misc.bits.notificationSound")
    }
}
