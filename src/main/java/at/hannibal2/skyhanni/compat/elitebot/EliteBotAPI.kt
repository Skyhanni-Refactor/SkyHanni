package at.hannibal2.skyhanni.compat.elitebot

import at.hannibal2.skyhanni.compat.elitebot.data.EliteBotContests
import at.hannibal2.skyhanni.compat.elitebot.data.EliteBotLeaderboardRank
import at.hannibal2.skyhanni.compat.elitebot.data.EliteBotPlayerWeights
import at.hannibal2.skyhanni.compat.elitebot.data.EliteBotWeights
import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.pests.PestType
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.http.Http
import at.hannibal2.skyhanni.utils.json.SkyHanniTypeAdapters

object EliteBotAPI {

    private const val URL = "https://api.elitebot.dev/"

    private val gson by lazy {
        ConfigManager.createBaseGsonBuilder()
            .registerTypeAdapter(CropType::class.java, SkyHanniTypeAdapters.CROP_TYPE.nullSafe())
            .registerTypeAdapter(PestType::class.java, SkyHanniTypeAdapters.PEST_TYPE.nullSafe())
            .create()
    }

    private suspend inline fun <reified T : Any> get(
        path: String,
        queries: Map<String, Any> = mapOf(),
    ): Result<T> {
        return try {
            Http.get("$URL$path", queries = queries) {
                if (isOk) {
                    Result.success(asJson<T>(gson))
                } else {
                    Result.failure(EliteBotError(asText()))
                }
            }
        } catch (e: Exception) {
            Result.failure(EliteBotError(e.message ?: "Unknown error"))
        }
    }

    suspend fun getLeaderboard(
        leaderboard: String,
        player: String,
        profile: String,
        includeUpcoming: Boolean = false,
        atRank: Int = -1
    ): Result<EliteBotLeaderboardRank> {
        return get(
            "Leaderboard/rank/$leaderboard/$player/$profile",
            mapOf(
                "includeUpcoming" to includeUpcoming,
                "atRank" to atRank
            )
        )
    }

    suspend fun getWeights(): Result<EliteBotWeights> {
        return get("Weights/All")
    }

    suspend fun getPlayerWeights(id: String): Result<EliteBotPlayerWeights> {
        return get("Weight/$id")
    }


    suspend fun getContests(): Result<EliteBotContests> {
        return get("Contests/at/now")
    }

    suspend fun sendContests(contests: Map<SimpleTimeMark, List<CropType>>): Result<Unit> {
        return try {
            val transformed = contests.map { (time, crops) -> time.toSeconds() to crops }.toMap()
            Http.post("${URL}contests/at/now", gson = gson, body = transformed) {
                if (isOk) {
                    Result.success(Unit)
                } else {
                    Result.failure(EliteBotError(asText()))
                }
            }
        } catch (e: Exception) {
            Result.failure(EliteBotError(e.message ?: "Unknown error"))
        }
    }


}
