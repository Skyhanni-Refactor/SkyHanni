package at.hannibal2.skyhanni.utils.json

import at.hannibal2.skyhanni.config.TrackerDisplayMode
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.features.fishing.trophy.TrophyRarity
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.pests.PestType
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NEUItems
import com.google.gson.TypeAdapter
import net.minecraft.item.ItemStack
import java.util.UUID

object SkyHanniTypeAdapters {
    val NEU_ITEMSTACK: TypeAdapter<ItemStack> = SimpleStringTypeAdapter(NEUItems::saveNBTData, NEUItems::loadNBTData)

    val UUID: TypeAdapter<UUID> = SimpleStringTypeAdapter(
        { this.toString() },
        { java.util.UUID.fromString(this) }
    )

    val INTERNAL_NAME: TypeAdapter<NEUInternalName> = SimpleStringTypeAdapter(
        { this.toString() },
        { this.asInternalName() }
    )

    val VEC_STRING: TypeAdapter<LorenzVec> = SimpleStringTypeAdapter(
        { "$x:$y:$z" },
        { LorenzVec.decodeFromString(this) }
    )

    val TROPHY_RARITY: TypeAdapter<TrophyRarity> = SimpleStringTypeAdapter(
        { name },
        { TrophyRarity.getByName(this) ?: error("Could not parse TrophyRarity from '$this'") }
    )

    val CROP_TYPE: TypeAdapter<CropType> = SimpleStringTypeAdapter(
        { name },
        { CropType.getByName(this) }
    )

    val PEST_TYPE: TypeAdapter<PestType> = SimpleStringTypeAdapter(
        { name },
        { PestType.getByName(this) }
    )

    val TRACKER_DISPLAY_MODE = SimpleStringTypeAdapter.forEnum<TrackerDisplayMode>()
    val ISLAND_TYPE = SimpleStringTypeAdapter.forEnum<IslandType>()
    val RARITY = SimpleStringTypeAdapter.forEnum<LorenzRarity>()

}
