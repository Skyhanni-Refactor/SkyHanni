package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.Perk
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.features.misc.update.UpdateManager
import at.hannibal2.skyhanni.features.nether.kuudra.KuudraAPI
import at.hannibal2.skyhanni.mixins.transformers.AccessorGuiEditSign
import at.hannibal2.skyhanni.test.TestBingo
import at.hannibal2.skyhanni.utils.ChatUtils.lastButtonClicked
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import at.hannibal2.skyhanni.utils.NEUItems.getItemStackOrNull
import at.hannibal2.skyhanni.utils.StringUtils.capAtMinecraftLength
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.datetime.SkyBlockTime
import at.hannibal2.skyhanni.utils.mc.McSound
import at.hannibal2.skyhanni.utils.mc.McSound.play
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.launchwrapper.Launch
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.FMLCommonHandler
import java.text.DecimalFormat
import java.text.SimpleDateFormat

object LorenzUtils {

    val connectedToHypixel get() = HypixelData.hypixelLive || HypixelData.hypixelAlpha

    val onHypixel get() = connectedToHypixel && Minecraft.getMinecraft().thePlayer != null

    val isOnAlphaServer get() = onHypixel && HypixelData.hypixelAlpha

    val inSkyBlock get() = onHypixel && HypixelData.skyBlock

    val inHypixelLobby get() = onHypixel && HypixelData.inLobby

    val inLimbo get() = onHypixel && HypixelData.inLimbo

    /**
     * Consider using [IslandType.isInIsland] instead
     */
    val skyBlockIsland get() = HypixelData.skyBlockIsland

    val skyBlockArea get() = if (inSkyBlock) HypixelData.skyBlockArea else null

    val inKuudraFight get() = inSkyBlock && KuudraAPI.inKuudra()

    val noTradeMode get() = HypixelData.noTrade

    val isStrandedProfile get() = inSkyBlock && HypixelData.stranded

    val isBingoProfile get() = inSkyBlock && (HypixelData.bingo || TestBingo.testBingo)

    val isIronmanProfile get() = inSkyBlock && HypixelData.ironman

    val lastWorldSwitch get() = HypixelData.joinedWorld

    val debug: Boolean = onHypixel && SkyHanniMod.feature.dev.debug.enabled

    fun SimpleDateFormat.formatCurrentTime(): String = this.format(System.currentTimeMillis())

    // TODO use derpy() on every use case
    val EntityLivingBase.baseMaxHealth: Int
        get() = this.getEntityAttribute(SharedMonsterAttributes.maxHealth).baseValue.toInt()

    // TODO create extension function
    fun formatPercentage(percentage: Double): String = formatPercentage(percentage, "0.00")

    fun formatPercentage(percentage: Double, format: String?): String =
        DecimalFormat(format).format(percentage * 100).replace(',', '.') + "%"

    fun consoleLog(text: String) {
        SkyHanniMod.consoleLog(text)
    }

    fun getSBMonthByName(month: String): Int {
        var monthNr = 0
        for (i in 1..12) {
            val monthName = SkyBlockTime.monthName(i)
            if (month == monthName) {
                monthNr = i
            }
        }
        return monthNr
    }

    fun fillTable(
        data: List<DisplayTableEntry>,
        padding: Int = 1,
        itemScale: Double = NEUItems.ITEM_FONT_SIZE,
    ): Renderable {
        val sorted = data.sortedByDescending { it.sort }

        val outerList = mutableListOf<List<Renderable>>()
        for (entry in sorted) {
            val item = entry.item.getItemStackOrNull()?.let {
                Renderable.itemStack(it, scale = itemScale)
            } ?: continue
            val left = Renderable.hoverTips(
                entry.left,
                tips = entry.hover,
                highlightsOnHoverSlots = entry.highlightsOnHoverSlots
            )
            val right = Renderable.string(entry.right)
            outerList.add(listOf(item, left, right))
        }
        return Renderable.table(outerList, xPadding = 5, yPadding = padding)
    }

    fun setTextIntoSign(text: String, line: Int = 0) {
        val gui = Minecraft.getMinecraft().currentScreen
        if (gui !is AccessorGuiEditSign) return
        gui.tileSign.signText[line] = ChatComponentText(text)
    }

    fun addTextIntoSign(addedText: String) {
        val gui = Minecraft.getMinecraft().currentScreen
        if (gui !is AccessorGuiEditSign) return
        val lines = gui.tileSign.signText
        val index = gui.editLine
        val text = lines[index].unformattedText + addedText
        lines[index] = ChatComponentText(text.capAtMinecraftLength(91))
    }

    fun colorCodeToRarity(colorCode: Char): String {
        return when (colorCode) {
            'f' -> "Common"
            'a' -> "Uncommon"
            '9' -> "Rare"
            '5' -> "Epic"
            '6' -> "Legendary"
            'd' -> "Mythic"
            'b' -> "Divine"
            '4' -> "Ultimate"
            else -> "Special"
        }
    }

    @Deprecated("do not use List<Any>, use List<Renderable> instead", ReplaceWith(""))
    inline fun <reified T : Enum<T>> MutableList<List<Any>>.addSelector(
        prefix: String,
        getName: (T) -> String,
        isCurrent: (T) -> Boolean,
        crossinline onChange: (T) -> Unit,
    ) {
        add(buildSelector<T>(prefix, getName, isCurrent, onChange))
    }

    @Deprecated("do not use List<Any>, use List<Renderable> instead", ReplaceWith(""))
    inline fun <reified T : Enum<T>> buildSelector(
        prefix: String,
        getName: (T) -> String,
        isCurrent: (T) -> Boolean,
        crossinline onChange: (T) -> Unit,
    ) = buildList {
        add(prefix)
        for (entry in enumValues<T>()) {
            val display = getName(entry)
            if (isCurrent(entry)) {
                add("§a[$display]")
            } else {
                add("§e[")
                add(Renderable.link("§e$display") {
                    onChange(entry)
                })
                add("§e]")
            }
            add(" ")
        }
    }

    @Deprecated("do not use List<Any>, use List<Renderable> instead", ReplaceWith(""))
    inline fun MutableList<List<Any>>.addButton(
        prefix: String,
        getName: String,
        crossinline onChange: () -> Unit,
        tips: List<String> = emptyList(),
    ) {
        val onClick = {
            if ((System.currentTimeMillis() - lastButtonClicked) > 150) { // funny thing happen if I don't do that
                onChange()
                McSound.CLICK.play()
                lastButtonClicked = System.currentTimeMillis()
            }
        }
        add(buildList {
            add(prefix)
            add("§a[")
            if (tips.isEmpty()) {
                add(Renderable.link("§e$getName", false, onClick))
            } else {
                add(Renderable.clickAndHover("§e$getName", tips, false, onClick))
            }
            add("§a]")
        })
    }

    fun GuiEditSign.isRancherSign(): Boolean {
        if (this !is AccessorGuiEditSign) return false

        val tileSign = (this as AccessorGuiEditSign).tileSign
        return (tileSign.signText[1].unformattedText.removeColor() == "^^^^^^"
            && tileSign.signText[2].unformattedText.removeColor() == "Set your"
            && tileSign.signText[3].unformattedText.removeColor() == "speed cap!")
    }

    fun IslandType.isInIsland() = inSkyBlock && skyBlockIsland == this

    fun inAnyIsland(vararg islandTypes: IslandType) = inSkyBlock && islandTypes.any { it.isInIsland() }

    fun GuiContainerEvent.SlotClickEvent.makeShiftClick() {
        if (this.clickedButton == 1 && slot?.stack?.getItemCategoryOrNull() == ItemCategory.SACK) return
        slot?.slotNumber?.let { slotNumber ->
            Minecraft.getMinecraft().playerController.windowClick(
                container.windowId, slotNumber, 0, 1, Minecraft.getMinecraft().thePlayer
            )
            isCanceled = true
        }
    }

    fun Int.derpy() = if (Perk.DOUBLE_MOBS_HP.isActive) this / 2 else this

    fun Int.ignoreDerpy() = if (Perk.DOUBLE_MOBS_HP.isActive) this * 2 else this

    inline fun <reified T : Enum<T>> enumValueOfOrNull(name: String): T? {
        val enums = enumValues<T>()
        return enums.firstOrNull { it.name == name }
    }

    inline fun <reified T : Enum<T>> enumValueOf(name: String) =
        enumValueOfOrNull<T>(name)
            ?: error("Unknown enum constant for ${enumValues<T>().first().name.javaClass.simpleName}: '$name'")

    inline fun <reified T : Enum<T>> enumJoinToPattern(noinline transform: (T) -> CharSequence = { it.name }) =
        enumValues<T>().joinToString("|", transform = transform)

    inline fun <reified T : Enum<T>> T.isAnyOf(vararg array: T): Boolean = array.contains(this)

    // TODO move to val by lazy
    fun isInDevEnvironment() = ((Launch.blackboard ?: mapOf())["fml.deobfuscatedEnvironment"] as Boolean?) ?: true

    fun shutdownMinecraft(reason: String? = null) {
        System.err.println("SkyHanni-${SkyHanniMod.version} forced the game to shutdown.")
        reason?.let {
            System.err.println("Reason: $it")
        }
        FMLCommonHandler.instance().handleExit(-1)
    }

    fun inAdvancedMiningIsland() =
        IslandType.DWARVEN_MINES.isInIsland() || IslandType.CRYSTAL_HOLLOWS.isInIsland() || IslandType.MINESHAFT.isInIsland()

    fun inMiningIsland() = IslandType.GOLD_MINES.isInIsland() || IslandType.DEEP_CAVERNS.isInIsland()
        || inAdvancedMiningIsland()

    fun isBetaVersion() = UpdateManager.isCurrentlyBeta()
}
