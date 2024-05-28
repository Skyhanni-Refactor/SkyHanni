package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.render.gui.TabListLineRenderEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimal
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.cleanPlayerName

@SkyHanniModule
object DungeonRankTabListColor {

    private val config get() = SkyHanniMod.feature.dungeon.tabList
    private val pattern = "§r(?<playerName>.*) §r§f\\(§r§d(?<className>.*) (?<classLevel>.*)§r§f\\)§r".toPattern()

    @HandleEvent
    fun onTabListText(event: TabListLineRenderEvent) {
        if (!isEnabled()) return

        pattern.matchMatcher(event.text) {
            val playerName = group("playerName")
            val split = playerName.split(" ")
            val sbLevel = split[0]
            val cleanName = split[1].cleanPlayerName(displayName = true)

            val className = group("className")
            val level = group("classLevel").romanToDecimal()
            val color = DungeonAPI.getColor(level)

            event.text = "$sbLevel $cleanName §7(§e$className $color$level§7)"
        }
    }

    fun isEnabled() = DungeonAPI.inDungeon() && config.coloredClassLevel
}
