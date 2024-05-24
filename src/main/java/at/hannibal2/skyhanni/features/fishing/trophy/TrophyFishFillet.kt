package at.hannibal2.skyhanni.features.fishing.trophy

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.events.item.SkyHanniToolTipEvent
import at.hannibal2.skyhanni.features.fishing.trophy.TrophyFishManager.getFilletValue
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.NEUItems.getPrice
import at.hannibal2.skyhanni.utils.NumberUtil
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import org.lwjgl.input.Keyboard

object TrophyFishFillet {

    @HandleEvent
    fun onTooltip(event: SkyHanniToolTipEvent) {
        if (!isEnabled()) return
        if (event.slot.inventory.name.contains("Sack")) return
        val internalName = event.itemStack.getInternalName().asString()
        val trophyFishName = internalName.substringBeforeLast("_")
            .replace("_", "").lowercase()
        val trophyRarityName = internalName.substringAfterLast("_")
        val info = TrophyFishManager.getInfo(trophyFishName) ?: return
        val rarity = TrophyRarity.getByName(trophyRarityName) ?: return
        val multiplier = if (Keyboard.KEY_LSHIFT.isKeyHeld()) event.itemStack.stackSize else 1
        val filletValue = info.getFilletValue(rarity) * multiplier
        // TODO use magma fish member
        val filletPrice = filletValue * SkyhanniItems.MAGMA_FISH().getPrice()
        event.toolTip.add("§7Fillet: §8${filletValue.addSeparators()} Magmafish §7(§6${NumberUtil.format(filletPrice)}§7)")
    }

    private fun isEnabled() = SkyBlockAPI.isConnected && SkyHanniMod.feature.fishing.trophyFishing.filletTooltip
}
