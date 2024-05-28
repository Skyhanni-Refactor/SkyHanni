package at.hannibal2.skyhanni.features.mining

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandArea
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.data.jsonobjects.repo.ParkourJson
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.inventory.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.render.gui.ReplaceItemEvent
import at.hannibal2.skyhanni.events.render.gui.SlotClickEvent
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.skyblock.IslandChangeEvent
import at.hannibal2.skyhanni.events.utils.ConfigFixEvent
import at.hannibal2.skyhanni.events.utils.ConfigLoadEvent
import at.hannibal2.skyhanni.events.utils.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ColourUtils.toChromaColour
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.ParkourHelper
import io.github.moulberry.notenoughupdates.util.Utils
import net.minecraft.client.player.inventory.ContainerLocalMenu

@SkyHanniModule
object DeepCavernsGuide {

    private val config get() = SkyHanniMod.feature.mining.deepCavernsGuide

    private var parkourHelper: ParkourHelper? = null
    private var show = false
    private var showStartIcon = false

    private val startIcon by lazy {
        val neuItem = SkyhanniItems.MAP().getItemStack()
        Utils.createItemStack(
            neuItem.item,
            "§bDeep Caverns Guide",
            "§8(From SkyHanni)",
            "",
            "§7Manually enable the ",
            "§7guide to the bottom",
            "§7of the Deep Caverns."
        )
    }

    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        parkourHelper?.reset()
        show = false
    }

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<ParkourJson>("DeepCavernsParkour")
        parkourHelper = ParkourHelper(
            data.locations,
            data.shortCuts,
            platformSize = 1.0,
            detectionRange = 3.5,
            depth = false,
            onEndReach = {
                show = false
            }
        )
        updateConfig()
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.rainbowColor, config.monochromeColor, config.lookAhead) {
            updateConfig()
        }
    }

    private fun updateConfig() {
        parkourHelper?.run {
            rainbowColor = config.rainbowColor.get()
            monochromeColor = config.monochromeColor.get().toChromaColour()
            lookAhead = config.lookAhead.get() + 1
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        showStartIcon = false
        if (!config.enabled) return
        if (event.inventoryName != "Lift") return
        if (!IslandArea.GUNPOWDER_MINES.isInside()) return
        showStartIcon = true

        event.inventoryItems[30]?.let {
            if (it.displayName != "§aObsidian Sanctuary") {
                if (!show) {
                    start()
                    ChatUtils.chat("Automatically enabling Deep Caverns Guide, helping you find the way to the bottom of the Deep Caverns and the path to Rhys.")
                }
            }
        }
    }

    private fun start() {
        show = true
        parkourHelper?.reset()
        if (parkourHelper == null) {
            ChatUtils.clickableChat(
                "DeepCavernsParkour missing in SkyHanni Repo! Try /shupdaterepo to fix it!",
                onClick = {
                    SkyHanniMod.repo.updateRepo()
                },
                prefixColor = "§c"
            )
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onInventoryClose(event: InventoryCloseEvent) {
        showStartIcon = false
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun replaceItem(event: ReplaceItemEvent) {
        if (show) return
        if (event.inventory is ContainerLocalMenu && showStartIcon && event.slot == 49) {
            event.replace(startIcon)
        }
    }

    @HandleEvent(priority = HandleEvent.HIGH)
    fun onSlotClick(event: SlotClickEvent) {
        if (showStartIcon && event.slotId == 49) {
            event.cancel()
            ChatUtils.chat("Manually enabled Deep Caverns Guide.")
            start()
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!config.enabled) return
        if (!show) return

        parkourHelper?.render(event)
    }

    @HandleEvent
    fun onConfigFix(event: ConfigFixEvent) {
        event.move(38, "mining.deepCavernsParkour", "mining.deepCavernsGuide")
    }
}
