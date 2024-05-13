package at.hannibal2.skyhanni.features.event.diana

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.Perk
import at.hannibal2.skyhanni.data.PetAPI
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.mc.McPlayer
import net.minecraft.item.ItemStack

object DianaAPI {

    fun hasSpadeInHand() = InventoryUtils.itemInHandId == SkyhanniItems.ANCESTRAL_SPADE()

    private fun isRitualActive() = Perk.MYTHOLOGICAL_RITUAL.isActive ||
        Perk.PERKPOCALYPSE.isActive

    fun hasGriffinPet() = PetAPI.isCurrentPet("Griffin")

    fun isDoingDiana() = IslandType.HUB.isInIsland() && isRitualActive() && hasSpadeInInventory()

    val ItemStack.isDianaSpade get() = getInternalName() == SkyhanniItems.ANCESTRAL_SPADE()

    private fun hasSpadeInInventory() = McPlayer.has(SkyhanniItems.ANCESTRAL_SPADE())
}
