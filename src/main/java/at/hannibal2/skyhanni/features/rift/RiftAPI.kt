package at.hannibal2.skyhanni.features.rift

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.skyblock.IslandArea
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.config.features.rift.RiftConfig
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName
import net.minecraft.item.ItemStack

object RiftAPI {

    // TODO doesnt work in rift tower with new changes
    fun inRift() = IslandType.THE_RIFT.isInIsland()

    val config: RiftConfig get() = SkyHanniMod.feature.rift

    // internal name -> motes
    var motesPrice = emptyMap<NEUInternalName, Double>()

    fun ItemStack.motesNpcPrice(): Double? {
        val baseMotes = motesPrice[getInternalName()] ?: return null
        val burgerStacks = config.motes.burgerStacks
        val pricePer = baseMotes + (burgerStacks * 5) * baseMotes / 100
        return pricePer * stackSize
    }

    fun inLivingCave() = IslandArea.LIVING_CAVE.isInside()
    fun inLivingStillness() = IslandArea.LIVING_STILLNESS.isInside()
    fun inStillgoreChateau() = IslandArea.STILLGORE_CHATEAU.isInside() || IslandArea.OUBLIETTE.isInside()

    fun inDreadfarm() = IslandArea.DREADFARM.isInside()
}
