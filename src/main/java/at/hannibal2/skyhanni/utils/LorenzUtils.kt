package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.HypixelAPI
import at.hannibal2.skyhanni.data.Perk
import at.hannibal2.skyhanni.utils.ChatUtils.lastButtonClicked
import at.hannibal2.skyhanni.utils.NEUItems.getItemStackOrNull
import at.hannibal2.skyhanni.utils.datetime.SkyBlockTime
import at.hannibal2.skyhanni.utils.mc.McSound
import at.hannibal2.skyhanni.utils.mc.McSound.play
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import java.text.SimpleDateFormat

object LorenzUtils {

    val debug: Boolean = HypixelAPI.onHypixel && SkyHanniMod.feature.dev.debug.enabled

    fun SimpleDateFormat.formatCurrentTime(): String = this.format(System.currentTimeMillis())

    // TODO use derpy() on every use case
    val EntityLivingBase.baseMaxHealth: Int
        get() = this.getEntityAttribute(SharedMonsterAttributes.maxHealth).baseValue.toInt()

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
}
