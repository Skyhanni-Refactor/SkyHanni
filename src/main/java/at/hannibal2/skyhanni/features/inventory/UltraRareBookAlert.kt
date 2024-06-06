package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.inventory.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.render.gui.ChestGuiOverlayRenderEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ColourUtils.withAlpha
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.mc.McScreen
import at.hannibal2.skyhanni.utils.mc.McSound
import at.hannibal2.skyhanni.utils.mc.McSound.play
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.RenderableUtils.renderXYAligned
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object UltraRareBookAlert {

    private val config get() = SkyHanniMod.feature.inventory.helper.enchanting
    private val dragonSound by lazy { McSound.create("mob.enderdragon.growl", 1f) }

    private val patternGroup = RepoPattern.group("data.enchanting")
    private val superpairsGui by patternGroup.pattern(
        "inventory.experimentstable.gui",
        "Superpairs.*"
    )
    private val ultraRarePattern by patternGroup.pattern(
        "inventory.experimentstable.ultrarare",
        "§d§kXX§5 ULTRA-RARE BOOK! §d§kXX"
    )
    private val bookPattern by patternGroup.pattern(
        "inventory.experimentstable.book",
        "§9(?<enchant>.*)"
    )

    private var enchantsFound = false

    private var lastNotificationTime = SimpleTimeMark.farPast()

    private fun notification(enchantsName: String) {
        lastNotificationTime = SimpleTimeMark.now()
        dragonSound.play()
        ChatUtils.chat("You have uncovered a §d§kXX§5 ULTRA-RARE BOOK! §d§kXX§e! You found: §9$enchantsName")
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onRenderOverlay(event: ChestGuiOverlayRenderEvent) {
        if (!config.ultraRareBookAlert) return
        if (!superpairsGui.matches(InventoryUtils.openInventoryName())) return
        if (lastNotificationTime.passedSince() > 5.seconds) return
        val gui = McScreen.asContainer ?: return

        GlStateManager.pushMatrix()
        GlStateManager.translate(0f, -150f, 500f)

        Renderable.drawInsideRoundedRect(
            Renderable.string("§d§kXX§5 ULTRA-RARE BOOK! §d§kXX", 1.5),
            Color(Color.DARK_GRAY.withAlpha(0), true),
            horizontalAlign = RenderUtils.HorizontalAlignment.CENTER,
            verticalAlign = RenderUtils.VerticalAlignment.CENTER,
        ).renderXYAligned(0, 125, gui.width, gui.height)

        GlStateManager.translate(0f, 150f, -500f)
        GlStateManager.popMatrix()
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        if (!config.ultraRareBookAlert) return
        if (enchantsFound) return
        if (!superpairsGui.matches(event.inventoryName)) return

        for (lore in event.inventoryItems.map { it.value.getLore() }) {
            val firstLine = lore.firstOrNull() ?: continue
            if (!ultraRarePattern.matches(firstLine)) continue
            val bookNameLine = lore.getOrNull(2) ?: continue
            bookPattern.matchMatcher(bookNameLine) {
                val enchantsName = group("enchant")
                notification(enchantsName)
                enchantsFound = true
            }
        }
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        enchantsFound = false
    }
}
