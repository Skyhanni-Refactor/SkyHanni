package at.hannibal2.skyhanni.api.skyblock

import java.util.EnumSet

class IslandTypeTag private constructor(val types: EnumSet<IslandType>){

    private constructor(vararg types: Any) : this(
        EnumSet.copyOf(
            types.flatMap {
                when (it) {
                    is IslandType -> listOf(it)
                    is IslandTypeTag -> it.types
                    else -> error("Invalid type: $it")
                }
            }
        )
    )

    fun inAny() = SkyBlockAPI.isConnected && types.contains(SkyBlockAPI.island)

    companion object {

        val PRIVATE_ISLAND = IslandTypeTag(IslandType.PRIVATE_ISLAND, IslandType.PRIVATE_ISLAND_GUEST)
        val IS_COLD = IslandTypeTag(IslandType.DWARVEN_MINES, IslandType.MINESHAFT)

        val NORMAL_MINING = IslandTypeTag(IslandType.GOLD_MINES, IslandType.DEEP_CAVERNS)
        val ADVANCED_MINING = IslandTypeTag(IS_COLD, IslandType.CRYSTAL_HOLLOWS)
        val MINING = IslandTypeTag(NORMAL_MINING, ADVANCED_MINING)

        val BITS_NOT_SHOWN = IslandTypeTag(IslandType.CATACOMBS, IslandType.KUUDRA_ARENA)
        val GEMS_NOT_SHOWN = IslandTypeTag(BITS_NOT_SHOWN, IslandType.THE_RIFT)

        val HOPPITY_DISALLOWED = IslandTypeTag(IslandType.THE_RIFT, IslandType.KUUDRA_ARENA, IslandType.CATACOMBS, IslandType.MINESHAFT)

        val SHOW_PARTY = IslandTypeTag(IslandType.DUNGEON_HUB, IslandType.KUUDRA_ARENA, IslandType.CRIMSON_ISLE)
    }

}
