package at.hannibal2.skyhanni.features.slayer.blaze

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.render.gui.GuiRenderEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.mc.McWorld
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.item.EntityArmorStand

@SkyHanniModule
object FirePillarDisplay {
    private val config get() = SkyHanniMod.feature.slayer.blazes

    /**
     * REGEX-TEST: §6§l2s §c§l8 hits
     */
    private val entityNamePattern by RepoPattern.pattern(
        "slayer.blaze.firepillar.entityname",
        "§6§l(?<seconds>.*)s §c§l8 hits"
    )

    private var display = ""

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!isEnabled()) return

        val seconds = McWorld.getEntitiesOf<EntityArmorStand>()
            .map { it.name }
            .matchFirst<String?>(entityNamePattern) {
                group("seconds")
            }

        display = seconds?.let {
            "§cFire Pillar: §b${seconds}s"
        } ?: ""
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!isEnabled()) return

        config.firePillarDisplayPosition.renderString(display, posLabel = "Fire Pillar")
    }

    fun isEnabled() = IslandType.CRIMSON_ISLE.isInIsland() && config.firePillarDisplay
}
