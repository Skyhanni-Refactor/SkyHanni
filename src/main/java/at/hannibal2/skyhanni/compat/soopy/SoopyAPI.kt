package at.hannibal2.skyhanni.compat.soopy

import at.hannibal2.skyhanni.compat.soopy.data.MiningEventBody
import at.hannibal2.skyhanni.compat.soopy.data.MiningEventResponse
import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.utils.http.Http

object SoopyAPI {

    private const val URL = "https://api.soopy.dev/"

    private val gson by lazy {
        ConfigManager.createBaseGsonBuilder()
            .create()
    }

    suspend fun getMiningEvent(): Result<MiningEventResponse> {
        return try {
            Http.get("$URL/skyblock/chevents/get") {
                if (isOk) {
                    val data = asJson<MiningEventResponse>(gson)
                    if (data.success) {
                        Result.success(data)
                    } else {
                        Result.failure(SoopyError(data.cause))
                    }
                } else {
                    Result.failure(SoopyError(asText()))
                }
            }
        } catch (e: Exception) {
            Result.failure(SoopyError(e.message ?: "Unknown error"))
        }
    }

    suspend fun postMiningEvent(body: MiningEventBody): Result<MiningEventResponse> {
        return try {
            Http.post(
                "$URL/skyblock/chevents/set",
                gson = gson,
                body = body
            ) {
                if (isOk) {
                    val data = asJson<MiningEventResponse>(gson)
                    if (data.success) {
                        Result.success(data)
                    } else {
                        Result.failure(SoopyError(data.cause))
                    }
                } else {
                    Result.failure(SoopyError(asText()))
                }
            }
        } catch (e: Exception) {
            Result.failure(SoopyError(e.message ?: "Unknown error"))
        }
    }

}
