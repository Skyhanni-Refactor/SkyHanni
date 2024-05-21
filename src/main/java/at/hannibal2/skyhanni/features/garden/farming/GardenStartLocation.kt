package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.click.CropClickEvent
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.test.GriffinUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText

object GardenStartLocation {

    private val config get() = GardenAPI.config.cropStartLocation

    fun setLocationCommand() {
        if (!GardenAPI.inGarden()) {
            ChatUtils.userError("This Command only works in the garden!")
            return
        }
        if (!config.enabled) {
            ChatUtils.chatAndOpenConfig(
                "This feature is disabled. Enable it in the config: §e/sh crop start location",
                GardenAPI.config::cropStartLocation
            )
            return
        }

        val startLocations = GardenAPI.storage?.cropStartLocations
        if (startLocations == null) {
            ChatUtils.userError("The config is not yet loaded, retry in a second.")
            return
        }

        val crop = GardenAPI.getCurrentlyFarmedCrop()
        if (crop == null) {
            ChatUtils.userError("Hold a crop specific farming tool in the hand!")
            return
        }

        startLocations[crop] = LocationUtils.playerLocation()
        ChatUtils.chat("You changed your Crop Start Location for ${crop.cropName}!")
    }

    @HandleEvent
    fun onCropClick(event: CropClickEvent) {
        if (!isEnabled()) return
        val startLocations = GardenAPI.storage?.cropStartLocations ?: return
        val crop = GardenAPI.getCurrentlyFarmedCrop() ?: return
        if (crop != GardenCropSpeed.lastBrokenCrop) return

        if (!startLocations.contains(crop)) {
            startLocations[crop] = LocationUtils.playerLocation()
            ChatUtils.chat("Auto updated your Crop Start Location for ${crop.cropName}")
        }
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return
        val startLocations = GardenAPI.storage?.cropStartLocations ?: return
        val crop = GardenAPI.cropInHand ?: return
        val location = startLocations[crop]?.add(-0.5, 0.5, -0.5) ?: return

        event.drawWaypointFilled(location, LorenzColor.WHITE.toColor())
        event.drawDynamicText(location, crop.cropName, 1.5)
    }

    fun isEnabled() = GardenAPI.inGarden() && config.enabled
}
