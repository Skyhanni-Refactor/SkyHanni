package at.hannibal2.skyhanni.features.event.diana

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.data.Perk
import at.hannibal2.skyhanni.data.PetAPI
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.events.diana.InquisitorFoundEvent
import at.hannibal2.skyhanni.events.entity.EntityEnterWorldEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.mc.McPlayer
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.item.ItemStack

@SkyHanniModule
object DianaAPI {

    private val spade = SkyhanniItems.ANCESTRAL_SPADE()

    fun hasSpadeInHand() = InventoryUtils.itemInHandId == spade

    private fun isRitualActive() = Perk.MYTHOLOGICAL_RITUAL.isActive ||
        Perk.PERKPOCALYPSE.isActive

    fun hasGriffinPet() = PetAPI.isCurrentPet("Griffin")

    fun isDoingDiana() = IslandType.HUB.isInIsland() && isRitualActive() && McPlayer.has(spade, true)

    val ItemStack.isDianaSpade get() = getInternalName() == spade

    @HandleEvent(onlyOnSkyblock = true)
    fun onJoinWorld(event: EntityEnterWorldEvent) {
        val entity = event.entity
        if (entity is EntityOtherPlayerMP && entity.name == "Minos Inquisitor") {
            InquisitorFoundEvent(entity).post()
        }
    }
}
