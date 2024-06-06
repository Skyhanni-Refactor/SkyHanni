package at.hannibal2.skyhanni.features.combat.endernodetracker

import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.utils.NEUInternalName

enum class EnderNode(
    val internalName: NEUInternalName,
    val displayName: String,
) {

    ENCHANTED_ENDSTONE(SkyhanniItems.ENCHANTED_ENDSTONE(), "§aEnchanted End Stone"),
    ENCHANTED_OBSIDIAN(SkyhanniItems.ENCHANTED_OBSIDIAN(), "§aEnchanted Obsidian"),
    ENCHANTED_ENDER_PEARL(SkyhanniItems.ENCHANTED_ENDER_PEARL(), "§aEnchanted Ender Pearl"),
    GRAND_EXP_BOTTLE(SkyhanniItems.GRAND_EXP_BOTTLE(), "§aGrand Experience Bottle"),
    TITANIC_EXP_BOTTLE(SkyhanniItems.TITANIC_EXP_BOTTLE(), "§9Titanic Experience Bottle"),
    END_STONE_SHULKER(SkyhanniItems.END_STONE_SHULKER(), "§9End Stone Shulker"),
    ENDSTONE_GEODE(SkyhanniItems.ENDSTONE_GEODE(), "§9End Stone Geode"),
    MAGIC_RUNE(SkyhanniItems.MAGIC_RUNE(1), "§d◆ Magical Rune I"),
    ENDER_GAUNTLET(SkyhanniItems.ENDER_GAUNTLET(), "§5Ender Gauntlet"),
    MITE_GEL(SkyhanniItems.MITE_GEL(), "§5Mite Gel"),
    SHRIMP_THE_FISH(SkyhanniItems.SHRIMP_THE_FISH(), "§cShrimp the Fish"),

    END_HELMET(SkyhanniItems.END_HELMET(), "§5Ender Helmet"),
    END_CHESTPLATE(SkyhanniItems.END_CHESTPLATE(), "§5Ender Chestplate"),
    END_LEGGINGS(SkyhanniItems.END_LEGGINGS(), "§5Ender Leggings"),
    END_BOOTS(SkyhanniItems.END_BOOTS(), "§5Ender Boots"),
    ENDER_NECKLACE(SkyhanniItems.ENDER_NECKLACE(), "§5Ender Necklace"),
    COMMON_ENDERMAN_PET(SkyhanniItems.ENDERMAN(0), "§fEnderman"),
    UNCOMMON_ENDERMAN_PET(SkyhanniItems.ENDERMAN(1), "§aEnderman"),
    RARE_ENDERMAN_PET(SkyhanniItems.ENDERMAN(2), "§9Enderman"),
    EPIC_ENDERMAN_PET(SkyhanniItems.ENDERMAN(3), "§5Enderman"),
    LEGENDARY_ENDERMAN_PET(SkyhanniItems.ENDERMAN(4), "§6Enderman")
}
