package at.hannibal2.skyhanni.features.rift.area.livingcave

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.render.gui.GuiRenderEvent
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.utils.CollectionUtils.addAsSingletonList
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RenderUtils.renderStringsAndItems
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getLivingMetalProgress
import at.hannibal2.skyhanni.utils.StringUtils.formatPercentage
import at.hannibal2.skyhanni.utils.mc.McPlayer
import net.minecraft.item.ItemStack

object LivingMetalSuitProgress {

    private val config get() = RiftAPI.config.area.livingCave.livingMetalSuitProgress
    private var display = emptyList<List<Any>>()
    private var progressMap = mapOf<ItemStack, Double?>()

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        config.position.renderStringsAndItems(
            display,
            posLabel = "Living Metal Armor Progress"
        )
    }

    private fun update() {
        display = drawDisplay()
    }

    fun drawDisplay(): List<List<Any>> = buildList {
        val piecesMaxed = progressMap.values.filterNotNull().count { it >= 1 }
        val isMaxed = piecesMaxed == 4

        if (progressMap.isEmpty()) return@buildList

        val totalProgress = progressMap.values.map { it ?: 1.0 }.average().roundTo(1)
        val formatPercentage = totalProgress.formatPercentage()
        addAsSingletonList("§7Living Metal Suit Progress: ${if (isMaxed) "§a§lMAXED!" else "§a$formatPercentage"}")

        if (config.compactWhenMaxed && isMaxed) return@buildList

        for ((stack, progress) in progressMap.entries.reversed()) {
            add(buildList {
                add("  §7- ")
                add(stack)
                add("${stack.displayName}: ")
                add(progress?.let {
                    drawProgressBar(progress) + " §b${progress.formatPercentage()}"
                } ?: "§cStart upgrading it!")
            })
        }
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        val old = progressMap
        progressMap = buildMap {
            for (armor in McPlayer.armor.filterNotNull()) {
                put(armor, armor.getLivingMetalProgress()?.toDouble()?.let {
                    it.coerceAtMost(100.0) / 100
                })
            }
        }
        if (old != progressMap) {
            update()
        }
    }

    private fun drawProgressBar(percentage: Double): String {
        val progressBarLength = 20
        val filledLength = (percentage * progressBarLength).toInt()

        val green = "§a"
        val grey = "§7"
        val reset = "§f"

        val progressBar = StringBuilder()
        progressBar.append(green)
        repeat(filledLength) { progressBar.append("|") }

        progressBar.append(grey)
        repeat(progressBarLength - filledLength) { progressBar.append("|") }

        progressBar.append(reset)
        return progressBar.toString()
    }

    fun isEnabled() = RiftAPI.inRift() && config.enabled
}
