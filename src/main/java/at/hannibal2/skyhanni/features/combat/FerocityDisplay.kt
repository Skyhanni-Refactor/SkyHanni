package at.hannibal2.skyhanni.features.combat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.minecraft.TabListUpdateEvent
import at.hannibal2.skyhanni.events.render.gui.GuiRenderEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

@SkyHanniModule
object FerocityDisplay {
    private val config get() = SkyHanniMod.feature.combat.ferocityDisplay

    /**
     * REGEX-TEST:  Ferocity: §r§c⫽14
     */
    private val ferocityPattern by RepoPattern.pattern(
        "combat.ferocity.tab",
        " Ferocity: §r§c⫽(?<stat>.*)"
    )

    private var display = ""

    @HandleEvent
    fun onTabListUpdate(event: TabListUpdateEvent) {
        if (!isEnabled()) return
        display = ""
        val stat = event.tabList.matchFirst(ferocityPattern) {
            group("stat")
        } ?: return

        display = "§c⫽$stat"

    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!isEnabled()) return

        config.position.renderString(display, posLabel = "Ferocity Display")
    }

    fun isEnabled() = SkyBlockAPI.isConnected && config.enabled
}
