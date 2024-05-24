package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.features.combat.damageindicator.BossType
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.utils.NEUInternalName

enum class PestType(
    val displayName: String,
    val damageIndicatorBoss: BossType,
    val spray: SprayType,
    val vinyl: VinylType,
    val internalName: NEUInternalName,
    val crop: CropType,
) {
    BEETLE(
        "Beetle",
        BossType.GARDEN_PEST_BEETLE,
        SprayType.DUNG,
        VinylType.NOT_JUST_A_PEST,
        SkyhanniItems.PEST_BEETLE_MONSTER(),
        CropType.NETHER_WART,
    ),
    CRICKET(
        "Cricket",
        BossType.GARDEN_PEST_CRICKET,
        SprayType.HONEY_JAR,
        VinylType.CRICKET_CHOIR,
        SkyhanniItems.PEST_CRICKET_MONSTER(),
        CropType.CARROT,
    ),
    EARTHWORM(
        "Earthworm",
        BossType.GARDEN_PEST_EARTHWORM,
        SprayType.COMPOST,
        VinylType.EARTHWORM_ENSEMBLE,
        SkyhanniItems.PEST_EARTHWORM_MONSTER(),
        CropType.MELON,
    ),
    FLY(
        "Fly",
        BossType.GARDEN_PEST_FLY,
        SprayType.DUNG,
        VinylType.PRETTY_FLY,
        SkyhanniItems.PEST_FLY_MONSTER(),
        CropType.WHEAT,
    ),
    LOCUST(
        "Locust",
        BossType.GARDEN_PEST_LOCUST,
        SprayType.PLANT_MATTER,
        VinylType.CICADA_SYMPHONY,
        SkyhanniItems.PEST_LOCUST_MONSTER(),
        CropType.POTATO,
    ),
    MITE(
        "Mite",
        BossType.GARDEN_PEST_MITE,
        SprayType.TASTY_CHEESE,
        VinylType.DYNAMITES,
        SkyhanniItems.PEST_MITE_MONSTER(),
        CropType.CACTUS,
    ),
    MOSQUITO(
        "Mosquito",
        BossType.GARDEN_PEST_MOSQUITO,
        SprayType.COMPOST,
        VinylType.BUZZIN_BEATS,
        SkyhanniItems.PEST_MOSQUITO_MONSTER(),
        CropType.SUGAR_CANE,
    ),
    MOTH(
        "Moth",
        BossType.GARDEN_PEST_MOTH,
        SprayType.HONEY_JAR,
        VinylType.WINGS_OF_HARMONY,
        SkyhanniItems.PEST_MOTH_MONSTER(),
        CropType.COCOA_BEANS,
    ),
    RAT(
        "Rat",
        BossType.GARDEN_PEST_RAT,
        SprayType.TASTY_CHEESE,
        VinylType.RODENT_REVOLUTION,
        SkyhanniItems.PEST_RAT_MONSTER(),
        CropType.PUMPKIN,
    ),
    SLUG(
        "Slug",
        BossType.GARDEN_PEST_SLUG,
        SprayType.PLANT_MATTER,
        VinylType.SLOW_AND_GROOVY,
        SkyhanniItems.PEST_SLUG_MONSTER(),
        CropType.MUSHROOM,
    ),
    ;

    companion object {
        fun getByNameOrNull(name: String): PestType? {
            return PestType.entries.firstOrNull { it.displayName.lowercase() == name }
        }

        fun getByName(name: String) = getByNameOrNull(name) ?: error("No valid pest type '$name'")
    }
}
