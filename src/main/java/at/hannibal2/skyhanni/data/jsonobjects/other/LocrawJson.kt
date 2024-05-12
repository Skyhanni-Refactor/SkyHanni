package at.hannibal2.skyhanni.data.jsonobjects.other

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LocrawJson(
    @Expose val server: String? = null,
    @Expose @SerializedName("gametype") val gameType: String? = null,
    @Expose @SerializedName("lobbyname") val lobbyName: String? = null,
    @Expose @SerializedName("lobbytype") val lobbyType: String? = null,
    @Expose val mode: String? = null,
    @Expose val map: String? = null,
)
