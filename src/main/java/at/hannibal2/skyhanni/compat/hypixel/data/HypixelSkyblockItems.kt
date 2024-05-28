package at.hannibal2.skyhanni.compat.hypixel.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class HypixelSkyblockItems(
    @Expose val items: List<Item>
) {

    data class Item(
        @Expose val id: String?,
        @Expose @SerializedName("npc_sell_price") val npcPrice: Double?,
        @Expose @SerializedName("motes_sell_price") val motesPrice: Double?
    )
}
