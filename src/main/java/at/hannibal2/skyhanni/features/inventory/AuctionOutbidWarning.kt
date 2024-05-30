package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.mc.McSound
import at.hannibal2.skyhanni.utils.mc.McSound.play
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object AuctionOutbidWarning {
    private val outbidPattern by RepoPattern.pattern(
        "auction.outbid",
        "§6\\[Auction].*§eoutbid you by.*§e§lCLICK"
    )

    @HandleEvent(onlyOnSkyblock = true)
    fun onChat(event: SkyHanniChatEvent) {
        if (!SkyHanniMod.feature.inventory.auctions.auctionOutbid) return
        if (!outbidPattern.matches(event.message)) return

        TitleManager.sendTitle("§cYou have been outbid!", 5.seconds, 3.6, 7.0f)
        McSound.BEEP.play()
    }
}
