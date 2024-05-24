package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.Gamemode
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.render.gui.GuiOverlayRenderEvent
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.datetime.TimeUnit
import at.hannibal2.skyhanni.utils.datetime.TimeUtils.format
import at.hannibal2.skyhanni.utils.mc.McSound
import at.hannibal2.skyhanni.utils.mc.McSound.play
import at.hannibal2.skyhanni.utils.mc.McWorld
import net.minecraft.client.Minecraft
import net.minecraft.entity.item.EntityArmorStand
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object FishingTimer {

    private val config get() = SkyHanniMod.feature.fishing.barnTimer
    private val barnLocation = LorenzVec(108, 89, -252)

    private var rightLocation = false
    private var currentCount = 0
    private var startTime = SimpleTimeMark.farPast()
    private var inHollows = false

    @HandleEvent(onlyOnSkyblock = true)
    fun onTick(event: ClientTickEvent) {
        if (!config.enabled) return

        if (event.repeatSeconds(3)) {
            rightLocation = isRightLocation()
        }

        if (!rightLocation) return

        if (event.isMod(5)) checkMobs()
        if (event.isMod(7)) tryPlaySound()
        if (config.manualResetTimer.isKeyHeld() && Minecraft.getMinecraft().currentScreen == null) {
            startTime = SimpleTimeMark.now()
        }
    }

    private fun tryPlaySound() {
        if (currentCount == 0) return

        val passedSince = startTime.passedSince()
        val barnTimerAlertTime = (config.alertTime * 1_000).milliseconds
        if (passedSince > barnTimerAlertTime && passedSince < barnTimerAlertTime + 3.seconds) {
            McSound.BEEP.play()
        }
    }

    private fun checkMobs() {
        val newCount = countMobs()

        if (currentCount == 0 && newCount > 0) {
            startTime = SimpleTimeMark.now()
        }

        currentCount = newCount
        if (newCount == 0) {
            startTime = SimpleTimeMark.farPast()
        }

        if (inHollows && newCount >= 60 && config.wormLimitAlert) {
            McSound.BEEP.play()
            TitleManager.sendTitle("§cWORM CAP FULL!!!", 2.seconds)
        }
    }

    private fun countMobs() = McWorld.getEntitiesOf<EntityArmorStand>().map(FishingAPI::seaCreatureCount).sum()

    private fun isRightLocation(): Boolean {
        inHollows = false

        if (config.forStranded && SkyBlockAPI.gamemode == Gamemode.STRANDED) return true

        if (config.crystalHollows && IslandType.CRYSTAL_HOLLOWS.isInIsland()) {
            inHollows = true
            return true
        }

        if (config.crimsonIsle && IslandType.CRIMSON_ISLE.isInIsland()) return true

        if (config.winterIsland && IslandType.WINTER.isInIsland()) return true

        if (!IslandType.THE_FARMING_ISLANDS.isInIsland()) {
            return LocationUtils.playerLocation().distance(barnLocation) < 50
        }

        return false
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onRenderOverlay(event: GuiOverlayRenderEvent) {
        if (!config.enabled) return
        if (!rightLocation) return
        if (currentCount == 0) return
        if (!FishingAPI.isFishing()) return

        val passedSince = startTime.passedSince()
        val barnTimerAlertTime = (config.alertTime * 1_000).milliseconds
        val color = if (passedSince > barnTimerAlertTime) "§c" else "§e"
        val timeFormat = passedSince.format(TimeUnit.MINUTE)
        val name = StringUtils.pluralize(currentCount, "sea creature")
        val text = "$color$timeFormat §8(§e$currentCount §b$name§8)"

        config.pos.renderString(text, posLabel = "BarnTimer")
    }
}
