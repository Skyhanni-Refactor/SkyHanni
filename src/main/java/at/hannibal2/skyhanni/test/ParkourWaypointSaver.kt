package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.minecraft.KeyPressEvent
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.ParkourHelper
import at.hannibal2.skyhanni.utils.RenderUtils.drawFilledBoundingBox_nea
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.math.BoundingBox
import at.hannibal2.skyhanni.utils.system.OS
import net.minecraft.client.Minecraft
import kotlin.time.Duration.Companion.milliseconds

object ParkourWaypointSaver {

    private val config get() = SkyHanniMod.feature.dev.waypoint
    private var timeLastSaved = SimpleTimeMark.farPast()
    private var locations = mutableListOf<LorenzVec>()
    private var parkourHelper: ParkourHelper? = null

    @HandleEvent
    fun onKeyClick(event: KeyPressEvent) {
        if (!SkyBlockAPI.isConnected && !config.parkourOutsideSB) return
        if (Minecraft.getMinecraft().currentScreen != null) return
        if (NEUItems.neuHasFocus()) return
        if (timeLastSaved.passedSince() < 250.milliseconds) return

        when (event.keyCode) {
            config.deleteKey -> {
                locations = locations.dropLast(1).toMutableList()
                update()
            }

            config.saveKey -> {
                val newLocation = LorenzVec.getBlockBelowPlayer()
                if (locations.isNotEmpty() && newLocation == locations.last()) return
                locations.add(newLocation)
                update()
            }
        }
    }

    private fun update() {
        locations.copyLocations()
        parkourHelper = ParkourHelper(locations, emptyList()).also {
            it.showEverything = true
            it.rainbowColor = true
        }
    }

    private fun MutableList<LorenzVec>.copyLocations() {
        val resultList = mutableListOf<String>()
        timeLastSaved = SimpleTimeMark.now()
        for (location in this) {
            val x = location.x.toString().replace(",", ".")
            val y = location.y.toString().replace(",", ".")
            val z = location.z.toString().replace(",", ".")
            resultList.add("\"$x:$y:$z\"")
        }
        OS.copyToClipboard(resultList.joinToString((",\n")))
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!SkyBlockAPI.isConnected && !config.parkourOutsideSB) return

        if (locations.size > 1) {
            parkourHelper?.render(event)
        } else {
            for (loc in locations) {
                val box = BoundingBox(
                    loc.x, loc.y, loc.z,
                    loc.x + 1, loc.y + 1, loc.z + 1
                ).expandToEdge()
                event.drawFilledBoundingBox_nea(box, LorenzColor.GREEN.toColor(), 1f)
            }
        }
    }
}
