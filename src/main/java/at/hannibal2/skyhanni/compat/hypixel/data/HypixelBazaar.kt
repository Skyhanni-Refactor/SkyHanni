package at.hannibal2.skyhanni.compat.hypixel.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class HypixelBazaar(
    @Expose val success: Boolean,
    @Expose val cause: String,
    @Expose val lastUpdated: Long,
    @Expose val products: Map<String, Product>,
) {

    data class Product(
        @Expose @SerializedName("product_id") val productId: String,
        @Expose @SerializedName("quick_status") val quickStatus: QuickStatus,
        @Expose @SerializedName("sell_summary") val sellSummary: List<Summary>,
        @Expose @SerializedName("buy_summary") val buySummary: List<Summary>,
    )

    class QuickStatus(
        @Expose val productId: String,
        @Expose val sellPrice: Double,
        @Expose val sellVolume: Long,
        @Expose val sellMovingWeek: Long,
        @Expose val sellOrders: Long,
        @Expose val buyPrice: Double,
        @Expose val buyVolume: Long,
        @Expose val buyMovingWeek: Long,
        @Expose val buyOrders: Long,
    ) {

        fun isEmpty(): Boolean =
            sellPrice == 0.0 &&
                sellVolume == 0L &&
                sellMovingWeek == 0L &&
                sellOrders == 0L &&
                buyPrice == 0.0 &&
                buyVolume == 0L &&
                buyMovingWeek == 0L &&
                buyOrders == 0L
    }

    data class Summary(
        @Expose val amount: Long,
        @Expose val pricePerUnit: Double,
        @Expose val orders: Long,
    )

}
