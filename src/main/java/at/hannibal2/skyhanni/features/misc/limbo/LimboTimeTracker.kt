package at.hannibal2.skyhanni.features.misc.limbo

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.HypixelAPI
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.chat.MessageSendToServerEvent
import at.hannibal2.skyhanni.events.hypixel.HypixelLocationEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.render.gui.GuiOverlayRenderEvent
import at.hannibal2.skyhanni.events.utils.ConfigFixEvent
import at.hannibal2.skyhanni.events.utils.DebugDataCollectEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.datetime.TimeUtils.format
import at.hannibal2.skyhanni.utils.math.BoundingBox
import at.hannibal2.skyhanni.utils.mc.McPlayer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@SkyHanniModule
object LimboTimeTracker {
    private val storage get() = ProfileStorageData.playerSpecific?.limbo
    private val config get() = SkyHanniMod.feature.misc

    private var limboJoinTime = SimpleTimeMark.farPast()
    private var inFakeLimbo = false
    private var shownPB = false
    private var oldPB: Duration = 0.seconds
    private var userLuck: Float = 0.0F
    private const val USER_LUCK_MULTIPLIER = 0.000810185F
    private const val FIRE_MULTIPLIER = 1.01F
    private var onFire = false

    private val bedwarsLobbyLimbo = BoundingBox(-662.0, 43.0, -76.0, -619.0, 86.0, -27.0)

    @HandleEvent
    fun onLocationChange(event: HypixelLocationEvent) {
        if (event.server == "limbo") {
            limboJoinTime = SimpleTimeMark.now()
            onFire = McPlayer.isOnFire
        } else if (!limboJoinTime.isFarPast()) {
            leaveLimbo()
        }
    }

    @HandleEvent
    fun onMessageSendToServer(event: MessageSendToServerEvent) {
        if (event.message.startsWith("/playtime") && inLimbo()) {
            event.cancel()
            printStats(true)
        }
    }

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        val personalBest = storage?.personalBest ?: 0
        if (inLimbo() && !shownPB && limboJoinTime.passedSince() >= personalBest.seconds && personalBest != 0) {
            shownPB = true
            oldPB = personalBest.seconds
            ChatUtils.chat("§d§lPERSONAL BEST§f! You've surpassed your previous record of §e$oldPB§f!")
            ChatUtils.chat("§fKeep it up!")
        }
        if (HypixelAPI.lobbyName?.startsWith("bedwarslobby") == true) {
            if (bedwarsLobbyLimbo.contains(McPlayer.pos)) {
                if (inFakeLimbo) return
                limboJoinTime = SimpleTimeMark.now()
                inFakeLimbo = true
            } else if (inFakeLimbo) {
                leaveLimbo()
                inFakeLimbo = false
            }
        }
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (!inLimbo()) return
        val duration = limboJoinTime.passedSince().format()
        config.showTimeInLimboPosition.renderString("§eIn limbo since §b$duration", posLabel = "Limbo Time Tracker")
    }

    private fun leaveLimbo() {
        if (!isEnabled()) return
        val passedSince = limboJoinTime.passedSince()
        val duration = passedSince.format()
        val currentPB = (storage?.personalBest ?: 0).seconds
        val oldLuck = storage?.userLuck ?: 0f
        if (passedSince > currentPB) {
            oldPB = currentPB
            storage?.personalBest = passedSince.toInt(DurationUnit.SECONDS)
            userLuck = ((storage?.personalBest ?: 0) * USER_LUCK_MULTIPLIER).roundTo(2)
            if (onFire) userLuck *= FIRE_MULTIPLIER
            ChatUtils.chat("§fYou were in Limbo for §e$duration§f! §d§lPERSONAL BEST§r§f!")
            if (oldPB != 0.seconds) {
                ChatUtils.chat("§fYour previous Personal Best was §e$oldPB.")
            }

        } else ChatUtils.chat("§fYou were in Limbo for §e$duration§f.")
        if (userLuck > oldLuck) {
            if (onFire) {
                ChatUtils.chat("§fYour §aPersonal Bests§f perk is now granting you §a+${userLuck.roundTo(2)}§c✴ §aSkyHanni User Luck§f! ")
            } else {
                ChatUtils.chat("§fYour §aPersonal Bests§f perk is now granting you §a+${userLuck.roundTo(2)}✴ SkyHanni User Luck§f!")
            }
            storage?.userLuck = userLuck
        }
        storage?.playtime = storage?.playtime?.plus(passedSince.toInt(DurationUnit.SECONDS)) ?: 0
        onFire = false
        shownPB = false
        limboJoinTime = SimpleTimeMark.farPast()
    }

    fun printStats(onlyPlaytime: Boolean = false) {
        val timeInLimbo: Int = if (inLimbo()) limboJoinTime.passedSince().inWholeSeconds.toInt() else 0
        val playtime: Int = if (inLimbo()) (storage?.playtime
            ?: 0) + limboJoinTime.passedSince().inWholeSeconds.toInt() else storage?.playtime ?: 0
        if (onlyPlaytime) {
            ChatUtils.chat("§aYou have ${playtime / 3600} hours and ${playtime % 3600 / 60} minutes playtime!", false)
        } else {
            val currentPB = storage?.personalBest ?: 0
            val userLuck = storage?.userLuck ?: 0f
            val limboPB: Int = if (currentPB < timeInLimbo) timeInLimbo else currentPB
            var luckString = tryTruncateFloat(userLuck.roundTo(2))
            if (userLuck > 0) luckString = "+$luckString"
            var firstMessage =
                "§fYour current PB is §e${limboPB.seconds}§f, granting you §a$luckString✴ SkyHanni User Luck§f!"
            val secondMessage = "§fYou have §e${playtime.seconds} §fof playtime!"
            if (userLuck == Float.POSITIVE_INFINITY || userLuck == Float.NEGATIVE_INFINITY) {
                firstMessage = "$firstMessage §Zwhat"
            }
            ChatUtils.chat(firstMessage)
            ChatUtils.chat(secondMessage)
        }
    }

    @HandleEvent
    fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Limbo")
        if (!inLimbo()) {
            event.addIrrelevant("not in limbo")
            return
        }

        event.addData {
            add("inLimbo: true")
            add("isLimboFake: $inFakeLimbo")
            add("since: ${limboJoinTime.passedSince()}")
        }
    }

    private fun isEnabled() = config.showTimeInLimbo

    private fun inLimbo() = HypixelAPI.connected && (HypixelAPI.server == "limbo" || inFakeLimbo)

    private fun tryTruncateFloat(input: Float): String {
        val string = input.toString()
        return if (string.endsWith(".0")) return string.dropLast(2)
        else string
    }

    @HandleEvent
    fun onConfigFix(event: ConfigFixEvent) {
        event.move(27, oldPath = "misc.limboTimePB", newPath = "#player.personalBest")
    }
}
