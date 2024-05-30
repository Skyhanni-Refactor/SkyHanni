package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.events.item.SkyHanniToolTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getBottleOfJyrreSeconds
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getSecondsHeld
import at.hannibal2.skyhanni.utils.datetime.TimeUtils.format
import net.minecraft.item.ItemStack
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object HeldTimeInLore {
    private val config get() = SkyHanniMod.feature.inventory

    @HandleEvent(onlyOnSkyblock = true)
    fun onTooltip(event: SkyHanniToolTipEvent) {
        if (!config.timeHeldInLore) return

        val seconds = event.itemStack.getSeconds() ?: return
        val formatted = seconds.seconds.format()

        event.toolTip.add(10, "§7Time Held: §b$formatted")
    }

    private fun ItemStack.getSeconds(): Int? = when (getInternalName()) {
        SkyhanniItems.NEW_BOTTLE_OF_JYRRE() -> getBottleOfJyrreSeconds()
        SkyhanniItems.DARK_CACAO_TRUFFLE() -> getSecondsHeld()
        else -> null
    }
}
