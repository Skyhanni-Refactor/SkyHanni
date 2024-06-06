package at.hannibal2.skyhanni.features.rift.area.stillgorechateau

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.jsonobjects.repo.RiftEffigiesJson
import at.hannibal2.skyhanni.events.minecraft.RawScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.utils.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.utils.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.datetime.TimeUtils
import at.hannibal2.skyhanni.utils.datetime.TimeUtils.format
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.mc.McWorld
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.item.EntityArmorStand
import kotlin.time.Duration.Companion.minutes

@SkyHanniModule
object RiftBloodEffigies {

    private val config get() = RiftAPI.config.area.stillgoreChateau.bloodEffigies

    private var locations: List<LorenzVec> = emptyList()
    private var effigiesTimes = cleanMap()

    private val patternGroup = RepoPattern.group("rift.area.stillgore.effegies")
    private val effigiesTimerPattern by patternGroup.pattern(
        "respawn",
        "§eRespawn §c(?<time>.*) §7\\(or click!\\)"
    )
    val heartsPattern by patternGroup.pattern(
        "heart",
        "Effigies: (?<hearts>((§[7c])?⧯)*)"
    )

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        effigiesTimes = cleanMap()
    }

    private fun cleanMap() = (0..5).associateWith { SimpleTimeMark.farPast() }

    @HandleEvent
    fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Rift Blood Effigies")

        if (!isEnabled()) {
            event.addIrrelevant("Not in Stillgore Château or not enabled ")
            return
        }
        event.addData {
            for ((number, duration) in effigiesTimes) {
                val time = duration.timeUntil().format()
                add("$number: $time ($duration)")
            }
        }
    }

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val newLocations = event.getConstant<RiftEffigiesJson>("RiftEffigies").locations
        if (newLocations.size != 6) {
            error("Invalid rift effigies size: ${newLocations.size} (expeced 6)")
        }
        locations = newLocations
    }

    @HandleEvent
    fun onRawScoreboardUpdate(event: RawScoreboardUpdateEvent) {
        if (!isEnabled()) return

        val line = event.newList.firstOrNull { it.startsWith("Effigies:") } ?: return
        val hearts = heartsPattern.matchMatcher(line) {
            group("hearts")
        } ?: return

        val split = hearts.split("§").drop(1)
        for ((index, s) in split.withIndex()) {
            val time = effigiesTimes[index]!!

            if (time.isInPast()) {
                if (s == "7") {
                    if (time.isFarPast()) {
                        ChatUtils.chat("Effigy #${index + 1} respawned!")
                        effigiesTimes = effigiesTimes.editCopy { this[index] = SimpleTimeMark.farPast() }
                    }
                } else {
                    if (time.isFarPast()) {
                        ChatUtils.chat("Effigy #${index + 1} is broken!")
                        val endTime = SimpleTimeMark.now() + 20.minutes
                        effigiesTimes = effigiesTimes.editCopy { this[index] = endTime }
                    }
                }
            }
        }
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return

        for (entity in McWorld.getEntitiesNearPlayer<EntityArmorStand>(6.0)) {
            effigiesTimerPattern.matchMatcher(entity.name) {
                val nearest = locations.minByOrNull { it.distanceSq(entity.getLorenzVec()) } ?: return
                val index = locations.indexOf(nearest)

                val string = group("time")
                val time = TimeUtils.getDuration(string)
                effigiesTimes = effigiesTimes.editCopy { this[index] = SimpleTimeMark.now() + time }
            }
        }
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return

        for ((index, location) in locations.withIndex()) {
            val name = "Effigy #${index + 1}"
            val duration = effigiesTimes[index]!!

            if (duration.isFarPast()) {
                if (config.unknownTime) {
                    event.drawWaypointFilled(location, LorenzColor.GRAY.toColor(), seeThroughBlocks = true)
                    event.drawDynamicText(location, "§7Unknown Time ($name)", 1.5)
                    continue
                }
            } else {
                if (duration.isFarPast()) {
                    event.drawWaypointFilled(location, LorenzColor.RED.toColor(), seeThroughBlocks = true)
                    event.drawDynamicText(location, "§cBreak $name!", 1.5)
                    continue
                }

                val timeUntil = duration.timeUntil()
                if (config.respawningSoon && timeUntil <= config.respwningSoonTime.minutes) {
                    event.drawWaypointFilled(location, LorenzColor.YELLOW.toColor(), seeThroughBlocks = true)
                    val time = timeUntil.format()
                    event.drawDynamicText(location, "§e$name is respawning §b$time", 1.5)
                    continue
                }
            }

            if (location.distanceToPlayer() < 5) {
                event.drawDynamicText(location, "§7$name", 1.5)
            }
        }
    }

    fun isEnabled() = RiftAPI.inRift() && config.enabled && RiftAPI.inStillgoreChateau()
}
