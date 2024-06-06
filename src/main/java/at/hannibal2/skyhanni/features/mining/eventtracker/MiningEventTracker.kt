package at.hannibal2.skyhanni.features.mining.eventtracker

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.BossbarAPI
import at.hannibal2.skyhanni.api.HypixelAPI
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.api.skyblock.IslandTypeTag
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.compat.soopy.SoopyAPI
import at.hannibal2.skyhanni.compat.soopy.data.MiningEventBody
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.minecraft.BossbarUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.skyblock.IslandChangeEvent
import at.hannibal2.skyhanni.events.utils.ConfigFixEvent
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.toDashlessUUID
import at.hannibal2.skyhanni.utils.datetime.TimeUtils
import at.hannibal2.skyhanni.utils.mc.McPlayer
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object MiningEventTracker {
    private val config get() = SkyHanniMod.feature.mining.miningEvent

    private val patternGroup = RepoPattern.group("mining.eventtracker")
    private val bossbarPassivePattern by patternGroup.pattern(
        "bossbar.passive",
        "§e§lPASSIVE EVENT (?<event>.+) §e§lRUNNING FOR §a§l(?<time>\\S+)§r"
    )
    private val bossbarActivePattern by patternGroup.pattern(
        "bossbar.active",
        "§e§lEVENT (?<event>.+) §e§lACTIVE IN (?<area>.+) §e§lfor §a§l(?<time>\\S+)§r"
    )

    // TODO add test messages
    private val eventStartedPattern by patternGroup.pattern(
        "started",
        "(?:§.)*\\s+(?:§.)+§l(?<event>.+) STARTED!"
    )
    private val eventEndedPattern by patternGroup.pattern(
        "ended",
        "(?:§.)*\\s+(?:§.)+§l(?<event>.+) ENDED!"
    )

    private val defaultCooldown = 1.minutes

    private var eventEndTime = SimpleTimeMark.farPast()
    private var lastSentEvent: MiningEventType? = null

    private var canRequestAt = SimpleTimeMark.farPast()

    var apiErrorCount = 0

    val apiError get() = apiErrorCount > 0

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        eventEndTime = SimpleTimeMark.farPast()
        lastSentEvent = null
    }

    @HandleEvent
    fun onBossbarChange(event: BossbarUpdateEvent) {
        if (!IslandTypeTag.ADVANCED_MINING.inAny()) return
        if (HypixelAPI.lastWorldChange.passedSince() < 5.seconds) return
        if (!eventEndTime.isInPast()) {
            return
        }

        bossbarPassivePattern.matchMatcher(event.bossbar) {
            sendData(group("event"), group("time"))
        }
        bossbarActivePattern.matchMatcher(event.bossbar) {
            sendData(group("event"), group("time"))
        }
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!IslandTypeTag.ADVANCED_MINING.inAny()) return

        eventStartedPattern.matchMatcher(event.message) {
            sendData(group("event"), null)
        }
        eventEndedPattern.matchMatcher(event.message) {
            lastSentEvent = null
        }
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!config.enabled) return
        if (!SkyBlockAPI.isConnected || (!config.outsideMining && !IslandTypeTag.ADVANCED_MINING.inAny())) return
        if (!canRequestAt.isInPast()) return

        fetchData()
    }

    private fun sendData(eventName: String, time: String?) {
        // TODO fix this via regex
        if (eventName == "SLAYER QUEST") return

        val eventType = MiningEventType.fromEventName(eventName) ?: run {
            if (!config.enabled) return
            ErrorManager.logErrorWithData(
                Exception("UnknownMiningEvent"), "Unknown mining event detected from string $eventName",
                "eventName" to eventName,
                "bossbar" to BossbarAPI.getBossbar(),
                "serverType" to SkyBlockAPI.island,
                "fromChat" to (time == null)
            )
            return
        }

        if (!IslandType.DWARVEN_MINES.isInIsland() && eventType.dwarvenSpecific) return

        if (lastSentEvent == eventType) return
        lastSentEvent = eventType

        val timeRemaining = if (time == null) {
            eventType.defaultLength
        } else {
            TimeUtils.getDuration(time)
        }
        eventEndTime = SimpleTimeMark.now() + timeRemaining

        val serverId = HypixelAPI.server ?: return

        if (apiError) {
            ChatUtils.debug("blocked sending mining event data: api error")
            return
        }
        val body = MiningEventBody(
            SkyBlockAPI.island,
            serverId,
            eventType,
            timeRemaining.inWholeMilliseconds,
            McPlayer.uuid.toDashlessUUID()
        )

        SkyHanniMod.coroutineScope.launch {
            SoopyAPI.postMiningEvent(body).onFailure {
                if (!config.enabled) return@launch
                ErrorManager.logErrorWithData(
                    it, "Sending mining event data was unsuccessful",
                    "data" to body
                )
            }
        }
    }

    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        if (apiError) {
            canRequestAt = SimpleTimeMark.now()
        }
    }

    private fun fetchData() {
        canRequestAt = SimpleTimeMark.now() + defaultCooldown
        SkyHanniMod.coroutineScope.launch {
            SoopyAPI.getMiningEvent().fold(
                {
                    apiErrorCount = 0
                    canRequestAt = SimpleTimeMark.now() + it.data.updateIn.milliseconds
                    MiningEventDisplay.updateData(it.data)
                },
                {
                    apiErrorCount++
                    canRequestAt = SimpleTimeMark.now() + 20.minutes
                    if (LorenzUtils.debug) {
                        ErrorManager.logErrorWithData(
                            it, "Receiving mining event data was unsuccessful",
                        )
                    }
                }
            )
        }
    }

    @HandleEvent
    fun onConfigFix(event: ConfigFixEvent) {
        event.transform(29, "mining.miningEvent.showType") { element ->
            if (element.asString == "BOTH") JsonPrimitive("ALL") else element
        }
    }
}
