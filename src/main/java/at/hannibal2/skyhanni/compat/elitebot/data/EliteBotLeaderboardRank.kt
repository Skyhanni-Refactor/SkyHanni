package at.hannibal2.skyhanni.compat.elitebot.data

import com.google.gson.annotations.Expose

data class EliteBotLeaderboardRank(
    @Expose val rank: Int,
    @Expose val amount: Double,
    @Expose val upcomingRank: Int,
    @Expose val upcomingPlayers: List<UpcomingPlayer>
) {

    data class UpcomingPlayer(
        @Expose val ign: String,
        @Expose val profile: String,
        @Expose val amount: Double
    )
}
