package at.hannibal2.skyhanni.features.garden.visitor

import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack

enum class VisitorReward(
    private val rawInternalName: String,
    val displayName: String,
) {
    FLOWERING_BOUQUET("FLOWERING_BOUQUET", "§9Flowering Bouquet"),
    OVERGROWN_GRASS("OVERGROWN_GRASS", "§9Overgrown Grass"),
    GREEN_BANDANA("GREEN_BANDANA", "§9Green Bandana"),
    DEDICATION("DEDICATION;4", "§9Dedication IV"),
    MUSIC_RUNE("MUSIC_RUNE;1", "§9Music Rune"),
    SPACE_HELMET("DCTR_SPACE_HELM", "§cSpace Helmet"),
    CULTIVATING("CULTIVATING;1", "§9Cultivating I"),
    REPLENISH("REPLENISH;1", "§9Replenish I"),
    DELICATE("DELICATE;5", "§9Delicate V"),
    ;

    private val internalName by lazy { rawInternalName.asInternalName() }
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
