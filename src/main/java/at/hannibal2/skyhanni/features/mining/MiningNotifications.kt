package at.hannibal2.skyhanni.features.mining

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.GetFromSackAPI
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.api.skyblock.IslandTypeTag
import at.hannibal2.skyhanni.data.MiningAPI.getCold
import at.hannibal2.skyhanni.data.MiningAPI.inColdIsland
import at.hannibal2.skyhanni.data.MiningAPI.lastColdReset
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.mining.ColdUpdateEvent
import at.hannibal2.skyhanni.events.utils.ConfigLoadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.PrimitiveItemStack.Companion.makePrimitiveStack
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.mc.McSound
import at.hannibal2.skyhanni.utils.mc.McSound.play
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object MiningNotifications {

    private val ASCENSION_ROPE = SkyhanniItems.ASCENSION_ROPE().makePrimitiveStack(1)

    enum class MiningNotificationList(val str: String, val notification: String) {
        MINESHAFT_SPAWN("§bGlacite Mineshaft", "§bMineshaft"),
        SCRAP("§9Suspicious Scrap", "§9Suspicious Scrap"),
        GOLDEN_GOBLIN("§6Golden Goblin", "§6Golden Goblin"),
        DIAMOND_GOBLIN("§bDiamond Goblin", "§bDiamond Goblin"),
        COLD("§bCold", "§bCold");

        override fun toString() = str
    }

    private val patternGroup = RepoPattern.group("mining.notifications")
    private val mineshaftSpawn by patternGroup.pattern(
        "mineshaft.spawn",
        "§5§lWOW! §r§aYou found a §r§bGlacite Mineshaft §r§aportal!"
    )
    private val scrapDrop by patternGroup.pattern(
        "scrapdrop",
        "§6§lEXCAVATOR! §r§fYou found a §r§9Suspicious Scrap§r§f!"
    )
    val goldenGoblinSpawn by patternGroup.pattern(
        "goblin.goldspawn",
        "§6A Golden Goblin has spawned!"
    )
    val diamondGoblinSpawn by patternGroup.pattern(
        "goblin.diamondspawn",
        "§6A §r§bDiamond Goblin §r§6has spawned!"
    )

    private val config get() = SkyHanniMod.feature.mining.notifications

    private var hasSentCold = false

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!IslandTypeTag.MINING.inAny()) return
        if (!config.enabled) return
        val message = event.message
        when {
            mineshaftSpawn.matches(message) -> sendNotification(MiningNotificationList.MINESHAFT_SPAWN)
            scrapDrop.matches(message) -> sendNotification(MiningNotificationList.SCRAP)
            goldenGoblinSpawn.matches(message) -> sendNotification(MiningNotificationList.GOLDEN_GOBLIN)
            diamondGoblinSpawn.matches(message) -> sendNotification(MiningNotificationList.DIAMOND_GOBLIN)
        }
    }

    @HandleEvent
    fun onColdUpdate(event: ColdUpdateEvent) {
        if (!inColdIsland()) return
        if (!config.enabled) return
        if (lastColdReset.passedSince() < 1.seconds) return

        if (event.cold >= config.coldThreshold.get() && !hasSentCold) {
            hasSentCold = true
            sendNotification(MiningNotificationList.COLD)
        }
        if (IslandType.MINESHAFT.isInIsland() && config.getAscensionRope && config.coldAmount == event.cold) {
            DelayedRun.runDelayed(0.5.seconds) {
                GetFromSackAPI.getFromChatMessageSackItems(ASCENSION_ROPE)
            }
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        hasSentCold = false
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.coldThreshold) {
            if (getCold() != config.coldThreshold.get()) hasSentCold = false
        }
    }

    private fun sendNotification(type: MiningNotificationList) {
        if (!config.notifications.contains(type)) return
        TitleManager.sendTitle(type.notification, 1500.milliseconds)
        if (!config.playSound) return
        McSound.PLING.play()
    }
}
