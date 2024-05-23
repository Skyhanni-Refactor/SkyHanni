package at.hannibal2.skyhanni.features.rift.everywhere

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.data.jsonobjects.repo.EnigmaSoulsJson
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.inventory.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.render.gui.BackgroundDrawnEvent
import at.hannibal2.skyhanni.events.render.gui.ReplaceItemEvent
import at.hannibal2.skyhanni.events.render.gui.SlotClickEvent
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.utils.RepositoryReloadEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.test.GriffinUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ColourUtils.toChromaColour
import at.hannibal2.skyhanni.utils.InventoryUtils.getAllItems
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import io.github.moulberry.notenoughupdates.util.Utils
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.player.inventory.ContainerLocalMenu
import net.minecraft.inventory.ContainerChest

object EnigmaSoulWaypoints {

    private val config get() = RiftAPI.config.enigmaSoulWaypoints
    private var inInventory = false
    private var soulLocations = mapOf<String, LorenzVec>()
    private val trackedSouls = mutableSetOf<String>()
    private val inventoryUnfound = mutableSetOf<String>()
    private var adding = true

    private val item by lazy {
        val neuItem = SkyhanniItems.SKYBLOCK_ENIGMA_SOUL().getItemStack()
        Utils.createItemStack(
            neuItem.item,
            "§5Toggle Missing",
            "§7Click here to toggle",
            "§7the waypoints for each",
            "§7missing souls on this page"
        )
    }

    @HandleEvent
    fun replaceItem(event: ReplaceItemEvent) {
        if (!isEnabled()) return

        if (inventoryUnfound.isEmpty()) return
        if (event.inventory is ContainerLocalMenu && inInventory && event.slot == 31) {
            event.replace(item)
        }
    }

    @HandleEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        inInventory = false
        if (!event.inventoryName.contains("Enigma Souls")) return
        inInventory = true

        for (stack in event.inventoryItems.values) {
            val split = stack.displayName.split("Enigma: ")
            if (split.size == 2 && stack.getLore().last() == "§8✖ Not completed yet!") {
                inventoryUnfound.add(split.last())
            }
        }
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
        inventoryUnfound.clear()
        adding = true
    }

    @HandleEvent(priority = HandleEvent.HIGH)
    fun onSlotClick(event: SlotClickEvent) {
        if (!inInventory || !isEnabled()) return

        if (event.slotId == 31 && inventoryUnfound.isNotEmpty()) {
            event.makePickblock()
            if (adding) {
                trackedSouls.addAll(inventoryUnfound)
                adding = false
            } else {
                trackedSouls.removeAll(inventoryUnfound)
                adding = true
            }
        }

        if (event.slot?.stack == null) return
        val split = event.slot.stack.displayName.split("Enigma: ")
        if (split.size == 2) {
            event.makePickblock()
            val name = split.last()
            if (soulLocations.contains(name)) {
                if (!trackedSouls.contains(name)) {
                    ChatUtils.chat("§5Tracking the $name Enigma Soul!", prefixColor = "§5")
                    trackedSouls.add(name)
                } else {
                    trackedSouls.remove(name)
                    ChatUtils.chat("§5No longer tracking the $name Enigma Soul!", prefixColor = "§5")
                }
            }
        }
    }

    @HandleEvent(priority = HandleEvent.LOWEST)
    fun onBackgroundDrawn(event: BackgroundDrawnEvent) {
        if (!isEnabled() || !inInventory) return

        if (event.gui !is GuiChest) return
        val guiChest = event.gui
        val chest = guiChest.inventorySlots as ContainerChest

        for ((slot, stack) in chest.getAllItems()) {
            for (soul in trackedSouls) {
                if (stack.displayName.removeColor().contains(soul)) {
                    slot highlight config.colour.toChromaColour()
                }
            }
        }
        if (!adding) {
            chest.inventorySlots[31] highlight config.colour.toChromaColour()
        }
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return
        for (soul in trackedSouls) {
            soulLocations[soul]?.let {
                event.drawWaypointFilled(it, config.colour.toChromaColour(), seeThroughBlocks = true, beacon = true)
                event.drawDynamicText(it.add(y = 1), "§5${soul.removeSuffix(" Soul")} Soul", 1.5)
            }
        }
    }

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<EnigmaSoulsJson>("EnigmaSouls")
        val areas = data.areas
        soulLocations = buildMap {
            for ((_, locations) in areas) {
                for (location in locations) {
                    this[location.name] = location.position
                }
            }
        }
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!isEnabled()) return
        val message = event.message.removeColor().trim()
        if (message == "You have already found that Enigma Soul!" || message == "SOUL! You unlocked an Enigma Soul!") {
            hideClosestSoul()
        }
    }

    private fun hideClosestSoul() {
        var closestSoul = ""
        var closestDistance = 8.0

        for ((soul, location) in soulLocations) {
            if (location.distanceToPlayer() < closestDistance) {
                closestSoul = soul
                closestDistance = location.distanceToPlayer()
            }
        }
        if (closestSoul in trackedSouls) {
            trackedSouls.remove(closestSoul)
            ChatUtils.chat("§5Found the $closestSoul Enigma Soul!", prefixColor = "§5")
        }
    }

    fun isEnabled() = RiftAPI.inRift() && config.enabled
}
