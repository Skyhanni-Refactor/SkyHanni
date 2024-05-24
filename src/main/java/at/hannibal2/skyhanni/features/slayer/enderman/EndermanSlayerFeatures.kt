package at.hannibal2.skyhanni.features.slayer.enderman

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.entity.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.minecraft.ServerBlockChangeEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.test.GriffinUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.ColourUtils.toChromaColour
import at.hannibal2.skyhanni.utils.ColourUtils.withAlpha
import at.hannibal2.skyhanni.utils.EntityUtils.canBeSeen
import at.hannibal2.skyhanni.utils.EntityUtils.getBlockInHand
import at.hannibal2.skyhanni.utils.ItemUtils.getSkullTexture
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LocationUtils.canBeSeen
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzLogger
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine
import at.hannibal2.skyhanni.utils.RenderUtils.drawColor
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.exactLocation
import at.hannibal2.skyhanni.utils.RenderUtils.exactPlayerEyeLocation
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.datetime.TimeUtils.format
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.init.Blocks
import kotlin.time.Duration.Companion.seconds

object EndermanSlayerFeatures {

    private val config get() = SkyHanniMod.feature.slayer.endermen
    private val beaconConfig get() = config.beacon
    private val endermenWithBeacons = mutableListOf<EntityEnderman>()
    private val flyingBeacons = mutableSetOf<EntityArmorStand>()
    private val nukekubiSkulls = mutableSetOf<EntityArmorStand>()
    private var sittingBeacon = mapOf<LorenzVec, SimpleTimeMark>()
    private val logger = LorenzLogger("slayer/enderman")
    private const val NUKEKUBI_SKULL_TEXTURE =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWIwNzU5NGUyZGYyNzM5MjFhNzdjMTAxZDBiZmRmYTExMTVhYmVkNWI5YjIwMjllYjQ5NmNlYmE5YmRiYjRiMyJ9fX0="

    @HandleEvent(onlyOnIsland = IslandType.THE_END)
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        val entity = event.entity
        if (entity in endermenWithBeacons || entity in flyingBeacons) return

        if (entity is EntityEnderman && showBeacon() && hasBeaconInHand(entity) && entity.canBeSeen(15.0)) {
            endermenWithBeacons.add(entity)
            logger.log("Added enderman with beacon at ${entity.getLorenzVec()}")
        }

        if (entity is EntityArmorStand) {
            if (showBeacon()) {
                val stack = entity.inventory[4] ?: return
                if (stack.name == "Beacon" && entity.canBeSeen(15.0)) {
                    flyingBeacons.add(entity)
                    RenderLivingEntityHelper.setEntityColor(
                        entity,
                        beaconConfig.beaconColor.toChromaColour().withAlpha(1)
                    ) {
                        beaconConfig.highlightBeacon
                    }
                    if (beaconConfig.showWarning) {
                        TitleManager.sendTitle("§4Beacon", 2.seconds)
                    }
                    logger.log("Added flying beacons at ${entity.getLorenzVec()}")
                }
            }

            if (config.highlightNukekebi && entity.inventory.any { it?.getSkullTexture() == NUKEKUBI_SKULL_TEXTURE } && entity !in nukekubiSkulls) {
                nukekubiSkulls.add(entity)
                RenderLivingEntityHelper.setEntityColor(
                    entity,
                    LorenzColor.GOLD.toColor().withAlpha(1)
                ) { config.highlightNukekebi }
                logger.log("Added Nukekubi skulls at ${entity.getLorenzVec()}")
            }
        }
    }

    private fun hasBeaconInHand(enderman: EntityEnderman) = enderman.getBlockInHand()?.block == Blocks.beacon

    private fun canSee(b: LorenzVec) = b.canBeSeen(15.0)

    private fun showBeacon() = beaconConfig.highlightBeacon || beaconConfig.showWarning || beaconConfig.showLine

    @HandleEvent(onlyOnIsland = IslandType.THE_END)
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {

        if (beaconConfig.highlightBeacon) {
            endermenWithBeacons.removeIf { it.isDead || !hasBeaconInHand(it) }

            endermenWithBeacons.map { it.getLorenzVec().add(-0.5, 0.2, -0.5) }
                .forEach { event.drawColor(it, beaconConfig.beaconColor.toChromaColour(), alpha = 0.5f) }
        }

        for ((location, time) in sittingBeacon) {
            if (location.distanceToPlayer() > 20) continue
            if (beaconConfig.showLine) {
                event.draw3DLine(
                    event.exactPlayerEyeLocation(),
                    location.add(0.5, 1.0, 0.5),
                    beaconConfig.lineColor.toChromaColour(),
                    beaconConfig.lineWidth,
                    true
                )
            }

            if (beaconConfig.highlightBeacon) {
                val duration = 5.seconds - time.passedSince()
                val durationFormat = duration.format(showMilliSeconds = true)
                event.drawColor(location, beaconConfig.beaconColor.toChromaColour(), alpha = 1f)
                event.drawWaypointFilled(location, beaconConfig.beaconColor.toChromaColour(), true, true)
                event.drawDynamicText(location.add(y = 1), "§4Beacon §b$durationFormat", 1.8)
            }
        }
        for (beacon in flyingBeacons) {
            if (beacon.isDead) continue
            if (beaconConfig.highlightBeacon) {
                val beaconLocation = event.exactLocation(beacon)
                event.drawDynamicText(beaconLocation.add(y = 1), "§4Beacon", 1.8)
            }

            if (beaconConfig.showLine) {
                val beaconLocation = event.exactLocation(beacon)
                event.draw3DLine(
                    event.exactPlayerEyeLocation(),
                    beaconLocation.add(0.5, 1.0, 0.5),
                    beaconConfig.lineColor.toChromaColour(),
                    beaconConfig.lineWidth,
                    true
                )
            }
        }

        config.highlightNukekebi
        for (skull in nukekubiSkulls) {
            if (!skull.isDead) {
                event.drawDynamicText(
                    skull.getLorenzVec().add(-0.5, 1.5, -0.5),
                    "§6Nukekubi Skull",
                    1.6,
                    ignoreBlocks = false
                )
            }
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.THE_END)
    fun onSecondPassed(event: SecondPassedEvent) {
        nukekubiSkulls.removeAll {
            if (it.isDead) {
                RenderLivingEntityHelper.removeEntityColor(it)
            }
            it.isDead
        }
        flyingBeacons.removeAll {
            if (it.isDead) {
                RenderLivingEntityHelper.removeEntityColor(it)
            }
            it.isDead
        }

        // Removing the beacon if It's still there after 7 sesconds.
        // This is just a workaround for the cases where the ServerBlockChangeEvent don't detect the beacon despawn info.
        val toRemove = sittingBeacon.filter { it.value.passedSince() > 7.seconds }
        if (toRemove.isNotEmpty()) {
            sittingBeacon = sittingBeacon.editCopy {
                toRemove.keys.forEach { remove(it) }
            }
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.THE_END)
    fun onBlockChange(event: ServerBlockChangeEvent) {
        if (!showBeacon()) return

        val location = event.location
        if (event.new == "beacon") {
            val armorStand = flyingBeacons.find { location.distance(it.getLorenzVec()) < 3 }
            if (armorStand != null) {
                flyingBeacons.remove(armorStand)
                RenderLivingEntityHelper.removeEntityColor(armorStand)
                sittingBeacon = sittingBeacon.editCopy { this[location] = SimpleTimeMark.now() }
                logger.log("Replaced flying beacon with sitting beacon at $location")
            }
        } else {
            if (location in sittingBeacon) {
                logger.log("Removed sitting beacon $location")
                sittingBeacon = sittingBeacon.editCopy { remove(location) }
            }
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        endermenWithBeacons.clear()
        flyingBeacons.clear()
        nukekubiSkulls.clear()
        sittingBeacon = emptyMap()
        logger.log("Reset everything (world change)")
    }
}
