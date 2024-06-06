package at.hannibal2.skyhanni.features.inventory.bazaar

import at.hannibal2.skyhanni.compat.hypixel.data.HypixelBazaar

data class BazaarData(
    val displayName: String,
    val sellOfferPrice: Double,
    val instantBuyPrice: Double,
    val product: HypixelBazaar.Product,
)
