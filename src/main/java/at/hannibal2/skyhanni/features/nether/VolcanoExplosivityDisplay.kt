package at.hannibal2.skyhanni.features.nether

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.events.minecraft.TabListUpdateEvent
import at.hannibal2.skyhanni.events.render.gui.GuiOverlayRenderEvent
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

object VolcanoExplosivityDisplay {

    private val config get() = SkyHanniMod.feature.crimsonIsle
    private val patternGroup = RepoPattern.group("crimson.volcano")

    /**
     * REGEX-TEST:  Volcano: §r§8INACTIVE
     */
    private val statusPattern by patternGroup.pattern(
        "tablistline",
        " *Volcano: (?<status>(?:§.)*\\S+)"
    )
    private var display = ""

    @HandleEvent
    fun onTabListUpdate(event: TabListUpdateEvent) {
        if (!isEnabled()) return
        event.tabList.matchFirst(statusPattern) {
            display = "§bVolcano Explosivity§7: ${group("status")}"
        }
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        config.positionVolcano.renderString(display, posLabel = "Volcano Explosivity")
    }

    private fun isEnabled() = IslandType.CRIMSON_ISLE.isInIsland() && config.volcanoExplosivity
}
