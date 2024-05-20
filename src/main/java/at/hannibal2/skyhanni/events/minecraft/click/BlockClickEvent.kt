package at.hannibal2.skyhanni.events.minecraft.click

import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.mc.McWorld.getBlockStateAt
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.Cancelable

@Cancelable
class BlockClickEvent(clickType: ClickType, val position: LorenzVec, itemInHand: ItemStack?) :
    WorldClickEvent(itemInHand, clickType) {

    val getBlockState by lazy { position.getBlockStateAt() }
}
