package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.Gamemode
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.GriffinUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.ColourUtils.toChromaColourInt
import at.hannibal2.skyhanni.utils.EntityUtils.hasSkullTexture
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.mc.McWorld
import at.hannibal2.skyhanni.utils.mc.McWorld.getBlockAt
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Blocks
import java.awt.Color

@SkyHanniModule
object ThunderSparksHighlight {

    private val config get() = SkyHanniMod.feature.fishing.thunderSpark
    private const val SPARK_TEXTURE =
        "ewogICJ0aW1lc3RhbXAiIDogMTY0MzUwNDM3MjI1NiwKICAicHJvZmlsZUlkIiA6ICI2MzMyMDgwZTY3YTI0Y2MxYjE3ZGJhNzZmM2MwMGYxZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJUZWFtSHlkcmEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2IzMzI4ZDNlOWQ3MTA0MjAzMjI1NTViMTcyMzkzMDdmMTIyNzBhZGY4MWJmNjNhZmM1MGZhYTA0YjVjMDZlMSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9"
    private val sparks = mutableListOf<EntityArmorStand>()

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!isEnabled()) return

        McWorld.getEntitiesOf<EntityArmorStand>().filter {
            it !in sparks && it.hasSkullTexture(SPARK_TEXTURE)
        }.forEach { sparks.add(it) }
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return

        val special = config.color
        val color = Color(special.toChromaColourInt(), true)

        val playerLocation = LocationUtils.playerLocation()
        for (spark in sparks) {
            if (spark.isDead) continue
            val sparkLocation = spark.getLorenzVec()
            val block = sparkLocation.getBlockAt()
            val seeThroughBlocks =
                sparkLocation.distanceToPlayer() < 6 && (block == Blocks.flowing_lava || block == Blocks.lava)
            event.drawWaypointFilled(
                sparkLocation.add(-0.5, 0.0, -0.5), color, extraSize = -0.25, seeThroughBlocks = seeThroughBlocks
            )
            if (sparkLocation.distance(playerLocation) < 10) {
                event.drawString(sparkLocation.add(y = 1.5), "Thunder Spark", seeThroughBlocks = seeThroughBlocks)
            }
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        sparks.clear()
    }

    private fun isEnabled() =
        (IslandType.CRIMSON_ISLE.isInIsland() || SkyBlockAPI.gamemode == Gamemode.STRANDED) && config.highlight
}
