package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.data.PetAPI
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.jsonobjects.repo.GardenJson
import at.hannibal2.skyhanni.events.garden.farming.GardenToolChangeEvent
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.minecraft.click.BlockClickEvent
import at.hannibal2.skyhanni.events.minecraft.click.CropClickEvent
import at.hannibal2.skyhanni.events.minecraft.packet.SendPacketEvent
import at.hannibal2.skyhanni.events.utils.ConfigLoadEvent
import at.hannibal2.skyhanni.events.utils.RepositoryReloadEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityCollectionStats
import at.hannibal2.skyhanni.features.garden.CropType.Companion.getCropType
import at.hannibal2.skyhanni.features.garden.composter.ComposterOverlay
import at.hannibal2.skyhanni.features.garden.contest.FarmingContestAPI
import at.hannibal2.skyhanni.features.garden.farming.GardenBestCropTime
import at.hannibal2.skyhanni.features.garden.farming.GardenCropSpeed
import at.hannibal2.skyhanni.features.garden.fortuneguide.FFGuideGUI
import at.hannibal2.skyhanni.features.garden.fortuneguide.FarmingItems
import at.hannibal2.skyhanni.features.garden.inventory.SkyMartCopperPrice
import at.hannibal2.skyhanni.features.garden.visitor.VisitorAPI
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateShopPrice
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addItemStack
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getItemRarityOrNull
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.RenderUtils.addItemIcon
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getCultivatingCounter
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getHoeCounter
import at.hannibal2.skyhanni.utils.math.BoundingBox
import at.hannibal2.skyhanni.utils.mc.McPlayer
import at.hannibal2.skyhanni.utils.mc.McWorld.checkProperty
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C09PacketHeldItemChange
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GardenAPI {

    var toolInHand: String? = null
    var itemInHand: ItemStack? = null
    var cropInHand: CropType? = null
    val mushroomCowPet
        get() = PetAPI.isCurrentPet("Mooshroom Cow") &&
            storage?.fortune?.farmingItems?.get(FarmingItems.MOOSHROOM_COW)
                ?.let { it.getItemRarityOrNull()?.isAtLeast(LorenzRarity.RARE) } ?: false
    private var inBarn = false
    val onBarnPlot get() = inBarn && inGarden()
    val storage get() = ProfileStorageData.profileSpecific?.garden
    val config get() = SkyHanniMod.feature.garden
    var totalAmountVisitorsExisting = 0
    var gardenExp: Long?
        get() = storage?.experience
        set(value) {
            value?.let {
                storage?.experience = it
            }
        }

    private const val GARDEN_OVERFLOW_XP = 10000

    private val barnArea = BoundingBox(35.5, 70.0, -4.5, -32.5, 100.0, -46.5)

    // TODO USE SH-REPO
    private val otherToolsList = listOf(
        "DAEDALUS_AXE",
        "BASIC_GARDENING_HOE",
        "ADVANCED_GARDENING_AXE",
        "BASIC_GARDENING_AXE",
        "ADVANCED_GARDENING_HOE",
        "ROOKIE_HOE",
        "BINGHOE"
    )

    @HandleEvent
    fun onSendPacket(event: SendPacketEvent) {
        if (!inGarden()) return
        if (event.packet !is C09PacketHeldItemChange) return
        checkItemInHand()
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (!inGarden()) return
        checkItemInHand()
        DelayedRun.runDelayed(500.milliseconds) {
            if (inGarden()) {
                checkItemInHand()
            }
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onTick(event: ClientTickEvent) {
        if (event.isMod(10, 1)) {
            inBarn = barnArea.contains(McPlayer.pos)

            // We ignore random hypixel moments
            Minecraft.getMinecraft().currentScreen ?: return
            checkItemInHand()
        }
    }

    // TODO use IslandChangeEvent
    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        DelayedRun.runDelayed(2.seconds) {
            if (inGarden()) {
                checkItemInHand()
            }
        }
    }

    private fun updateGardenTool() {
        GardenToolChangeEvent(cropInHand, itemInHand).post()
    }

    private fun checkItemInHand() {
        val toolItem = McPlayer.heldItem
        val crop = toolItem?.getCropType()
        val newTool = getToolInHand(toolItem, crop)
        if (toolInHand != newTool) {
            toolInHand = newTool
            cropInHand = crop
            itemInHand = toolItem
            updateGardenTool()
        }
    }

    private fun getToolInHand(toolItem: ItemStack?, crop: CropType?): String? {
        if (crop != null) return crop.cropName

        val internalName = toolItem?.getInternalName() ?: return null
        return if (isOtherTool(internalName)) internalName.asString() else null
    }

    private fun isOtherTool(internalName: NEUInternalName): Boolean {
        return internalName.asString() in otherToolsList
    }

    fun inGarden() = IslandType.GARDEN.isInIsland()

    fun isCurrentlyFarming() = inGarden() && GardenCropSpeed.averageBlocksPerSecond > 0.0 && hasFarmingToolInHand()

    fun hasFarmingToolInHand() = McPlayer.heldItem?.let {
        val crop = it.getCropType()
        getToolInHand(it, crop) != null
    } ?: false

    fun ItemStack.getCropType(): CropType? {
        val internalName = getInternalName()
        return CropType.entries.firstOrNull { internalName.startsWith(it.toolName) }
    }

    fun readCounter(itemStack: ItemStack): Long = itemStack.getHoeCounter() ?: itemStack.getCultivatingCounter() ?: -1L

    @Deprecated("use renderable list instead", ReplaceWith(""))
    fun MutableList<Any>.addCropIcon(
        crop: CropType,
        scale: Double = NEUItems.ITEM_FONT_SIZE,
        highlight: Boolean = false,
    ) =
        addItemIcon(crop.icon.copy(), highlight, scale = scale)

    // TODO rename to addCropIcon
    fun MutableList<Renderable>.addCropIconRenderable(
        crop: CropType,
        scale: Double = NEUItems.ITEM_FONT_SIZE,
        highlight: Boolean = false,
    ) {
        addItemStack(crop.icon.copy(), highlight = highlight, scale = scale)
    }

    fun hideExtraGuis() = ComposterOverlay.inInventory || AnitaMedalProfit.inInventory ||
        SkyMartCopperPrice.inInventory || FarmingContestAPI.inInventory || VisitorAPI.inInventory ||
        FFGuideGUI.isInGui() || ChocolateShopPrice.inInventory || ChocolateFactoryAPI.inChocolateFactory ||
        ChocolateFactoryAPI.chocolateFactoryPaused || HoppityCollectionStats.inInventory

    fun clearCropSpeed() {
        storage?.cropsPerSecond?.clear()
        GardenBestCropTime.reset()
        updateGardenTool()
        ChatUtils.chat("Manually reset all crop speed data!")
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        GardenBestCropTime.reset()
    }

    fun getCurrentlyFarmedCrop(): CropType? {
        val brokenCrop = if (toolInHand != null) GardenCropSpeed.lastBrokenCrop else null
        return cropInHand ?: brokenCrop
    }

    private var lastLocation: LorenzVec? = null

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onBlockClick(event: BlockClickEvent) {
        val blockState = event.getBlockState
        val cropBroken = blockState.getCropType() ?: return
        val isBaby = blockState.checkProperty<Int, PropertyInteger>("age") { it == 0 }
        if (cropBroken.multiplier == 1 && isBaby) return

        val position = event.position
        if (lastLocation == position) {
            return
        }

        lastLocation = position
        CropClickEvent(position, cropBroken, blockState, event.clickType, event.itemInHand).post()
    }

    fun getExpForLevel(requestedLevel: Int): Long {
        var totalExp = 0L
        var tier = 0
        for (tierExp in gardenExperience) {
            totalExp += tierExp
            tier++
            if (tier == requestedLevel) {
                return totalExp
            }
        }

        while (tier < requestedLevel) {
            totalExp += GARDEN_OVERFLOW_XP
            tier++
            if (tier == requestedLevel) {
                return totalExp
            }
        }
        return 0
    }

    fun getGardenLevel(overflow: Boolean = true): Int {
        val gardenExp = this.gardenExp ?: return 0
        var tier = 0
        var totalExp = 0L
        for (tierExp in gardenExperience) {
            totalExp += tierExp
            if (totalExp > gardenExp) {
                return tier
            }
            tier++
        }
        if (overflow) {
            totalExp += GARDEN_OVERFLOW_XP

            while (totalExp < gardenExp) {
                tier++
                totalExp += GARDEN_OVERFLOW_XP
            }
        }
        return tier
    }

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<GardenJson>("Garden")
        gardenExperience = data.gardenExp
        totalAmountVisitorsExisting = data.visitors.size
    }

    private var gardenExperience = listOf<Int>()
}
