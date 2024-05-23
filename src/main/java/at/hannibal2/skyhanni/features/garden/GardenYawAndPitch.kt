package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.api.HypixelAPI
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.config.enums.OutsideSbFeature
import at.hannibal2.skyhanni.events.garden.farming.GardenToolChangeEvent
import at.hannibal2.skyhanni.events.render.gui.GuiRenderEvent
import at.hannibal2.skyhanni.events.utils.ConfigFixEvent
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraft.client.Minecraft
import kotlin.time.Duration.Companion.seconds

object GardenYawAndPitch {

    private val config get() = GardenAPI.config.yawPitchDisplay
    private var lastChange = SimpleTimeMark.farPast()
    private var lastYaw = 0f
    private var lastPitch = 0f

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!HypixelAPI.onHypixel) return
        if (!isEnabled()) return
        if (GardenAPI.hideExtraGuis()) return
        if (GardenAPI.toolInHand == null && !config.showWithoutTool) return

        val player = Minecraft.getMinecraft().thePlayer

        var yaw = player.rotationYaw % 360
        if (yaw < 0) yaw += 360
        if (yaw > 180) yaw -= 360
        val pitch = player.rotationPitch

        if (yaw != lastYaw || pitch != lastPitch) {
            lastChange = SimpleTimeMark.now()
        }
        lastYaw = yaw
        lastPitch = pitch

        if (!config.showAlways && lastChange.passedSince() > config.timeout.seconds) return

        val yawText = yaw.roundTo(config.yawPrecision).toBigDecimal().toPlainString()
        val pitchText = pitch.roundTo(config.pitchPrecision).toBigDecimal().toPlainString()
        val displayList = listOf(
            "§aYaw: §f$yawText",
            "§aPitch: §f$pitchText",
        )
        if (GardenAPI.inGarden()) {
            config.pos.renderStrings(displayList, posLabel = "Yaw and Pitch")
        } else {
            config.posOutside.renderStrings(displayList, posLabel = "Yaw and Pitch")
        }
    }

    @HandleEvent
    fun onGardenToolChange(event: GardenToolChangeEvent) {
        lastChange = SimpleTimeMark.farPast()
    }

    private fun isEnabled() =
        config.enabled && ((OutsideSbFeature.YAW_AND_PITCH.isSelected() && !SkyBlockAPI.isConnected) ||
            (SkyBlockAPI.isConnected && (GardenAPI.inGarden() || config.showOutsideGarden)))

    @HandleEvent
    fun onConfigFix(event: ConfigFixEvent) {
        event.move(18, "garden.yawPitchDisplay.showEverywhere", "garden.yawPitchDisplay.showOutsideGarden")
    }
}
