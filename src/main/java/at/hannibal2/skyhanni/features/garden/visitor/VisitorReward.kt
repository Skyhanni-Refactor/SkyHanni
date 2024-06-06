package at.hannibal2.skyhanni.features.garden.visitor

import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack

enum class VisitorReward(
    private val internalName: NEUInternalName,
    val displayName: String,
) {
    FLOWERING_BOUQUET(SkyhanniItems.FLOWERING_BOUQUET(), "§9Flowering Bouquet"),
    OVERGROWN_GRASS(SkyhanniItems.OVERGROWN_GRASS(), "§9Overgrown Grass"),
    GREEN_BANDANA(SkyhanniItems.GREEN_BANDANA(), "§9Green Bandana"),
    DEDICATION(SkyhanniItems.DEDICATION(4), "§9Dedication IV"),
    MUSIC_RUNE(SkyhanniItems.MUSIC_RUNE(1), "§9Music Rune"),
    SPACE_HELMET(SkyhanniItems.DCTR_SPACE_HELM(), "§cSpace Helmet"),
    CULTIVATING(SkyhanniItems.CULTIVATING(1), "§9Cultivating I"),
    REPLENISH(SkyhanniItems.REPLENISH(1), "§9Replenish I"),
    DELICATE(SkyhanniItems.DELICATE(5), "§9Delicate V"),
    ;
    val itemStack by lazy { internalName.getItemStack() }
    // TODO use this instead of hard coded item names once moulconfig no longer calls toString before the neu repo gets loaded
//     val displayName by lazy { itemStack.nameWithEnchantment ?: internalName.asString() }

    companion object {
        fun getByInternalName(internalName: NEUInternalName) = entries.firstOrNull { it.internalName == internalName }
    }

    override fun toString(): String {
        return displayName
    }
}
