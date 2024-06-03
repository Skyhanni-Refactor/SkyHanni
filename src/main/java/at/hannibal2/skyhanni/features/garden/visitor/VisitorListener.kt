package at.hannibal2.skyhanni.features.garden.visitor

import at.hannibal2.skyhanni.api.HypixelAPI
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.config.features.garden.visitor.VisitorConfig
import at.hannibal2.skyhanni.events.entity.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.garden.visitor.VisitorOpenEvent
import at.hannibal2.skyhanni.events.garden.visitor.VisitorRenderEvent
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.inventory.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.item.SkyHanniToolTipEvent
import at.hannibal2.skyhanni.events.minecraft.TabListUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.packet.SendPacketEvent
import at.hannibal2.skyhanni.events.render.gui.GuiKeyPressEvent
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.utils.ProfileJoinEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.visitor.VisitorAPI.ACCEPT_SLOT
import at.hannibal2.skyhanni.features.garden.visitor.VisitorAPI.INFO_SLOT
import at.hannibal2.skyhanni.features.garden.visitor.VisitorAPI.lastClickedNpc
import at.hannibal2.skyhanni.mixins.transformers.AccessorGuiContainer
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzLogger
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.exactLocation
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.network.play.client.C02PacketUseEntity
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object VisitorListener {
    private val offersAcceptedPattern by RepoPattern.pattern(
        "garden.visitor.offersaccepted",
        "§7Offers Accepted: §a(?<offersAccepted>\\d+)"
    )

    private val config get() = VisitorAPI.config

    private val logger = LorenzLogger("garden/visitors/listener")

    @HandleEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        VisitorAPI.reset()
    }

    // TODO make event
    @HandleEvent
    fun onSendEvent(event: SendPacketEvent) {
        val packet = event.packet
        if (packet !is C02PacketUseEntity) return

        val theWorld = Minecraft.getMinecraft().theWorld
        val entity = packet.getEntityFromWorld(theWorld) ?: return
        val entityId = entity.entityId

        lastClickedNpc = entityId
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onTabListUpdate(event: TabListUpdateEvent) {
        val hasVisitorInfo = event.tabList.any { VisitorAPI.visitorCountPattern.matches(it) }
        if (!hasVisitorInfo) return

        val visitorsInTab = VisitorAPI.visitorsInTabList(event.tabList)

        if (HypixelAPI.lastWorldChange.passedSince() > 2.seconds) {
            VisitorAPI.getVisitors().forEach {
                val name = it.visitorName
                val removed = name !in visitorsInTab
                if (removed) {
                    logger.log("Removed old visitor: '$name'")
                    VisitorAPI.removeVisitor(name)
                }
            }
        }

        for (name in visitorsInTab) {
            VisitorAPI.addVisitor(name)
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        val npcItem = event.inventoryItems[INFO_SLOT] ?: return
        val lore = npcItem.getLore()
        if (!VisitorAPI.isVisitorInfo(lore)) return

        val offerItem = event.inventoryItems[ACCEPT_SLOT] ?: return
        if (offerItem.name != "§aAccept Offer") return

        VisitorAPI.inInventory = true

        val visitorOffer = VisitorAPI.VisitorOffer(offerItem)

        var name = npcItem.name
        if (name.length == name.removeColor().length + 4) {
            name = name.substring(2)
        }

        val visitor = VisitorAPI.getOrCreateVisitor(name) ?: return

        visitor.offersAccepted = offersAcceptedPattern.matchMatcher(lore[3]) { group("offersAccepted").toInt() }
        visitor.entityId = lastClickedNpc
        visitor.offer = visitorOffer
        VisitorOpenEvent(visitor).post()
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        VisitorAPI.inInventory = false
    }

    @HandleEvent
    fun onKeybind(event: GuiKeyPressEvent) {
        if (!VisitorAPI.inInventory) return
        if (!config.acceptHotkey.isKeyHeld()) return
        val inventory = event.guiContainer as? AccessorGuiContainer
            ?: return
        inventory as GuiContainer
        val slot = inventory.inventorySlots.getSlot(29)
        inventory.handleMouseClick_skyhanni(slot, slot.slotIndex, 0, 0)
    }

    @HandleEvent(priority = HandleEvent.HIGH)
    fun onTooltip(event: SkyHanniToolTipEvent) {
        if (!GardenAPI.onBarnPlot) return
        if (!VisitorAPI.inInventory) return
        val visitor = VisitorAPI.getVisitor(lastClickedNpc) ?: return
        GardenVisitorFeatures.onTooltip(visitor, event.itemStack, event.toolTip)
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (!GardenAPI.onBarnPlot) return
        if (config.highlightStatus != VisitorConfig.HighlightMode.NAME && config.highlightStatus != VisitorConfig.HighlightMode.BOTH) return

        val entity = event.entity
        if (entity is EntityArmorStand && entity.name == "§e§lCLICK") {
            event.cancel()
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!GardenAPI.onBarnPlot) return
        if (config.highlightStatus != VisitorConfig.HighlightMode.NAME && config.highlightStatus != VisitorConfig.HighlightMode.BOTH) return

        for (visitor in VisitorAPI.getVisitors()) {
            visitor.getNameTagEntity()?.let {
                if (it.distanceToPlayer() > 15) return@let
                VisitorRenderEvent(visitor, event.exactLocation(it), event).post()
            }
        }
    }
}
