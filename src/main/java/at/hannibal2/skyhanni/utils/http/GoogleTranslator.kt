package at.hannibal2.skyhanni.utils.http

import com.google.gson.Gson

object GoogleTranslator {

    private const val URL = "https://translate.google.com/translate_a/single"
    private val GSON = Gson()

    data class Sentence(val trans: String, val orig: String)
    data class Translation(val sentences: List<Sentence>, val src: String) {

        val text: String get() = sentences.joinToString(" ") { it.trans }
    }

    open class TranslationError(val error: String, override val message: String) : Error(message) {
        override fun toString(): String = "$error: $message"
    }

    class SameLanguageError(val lang: String) :
        TranslationError("SameLanguage", "The source and target languages are the same ($lang)")

    suspend fun translate(text: String, from: String, to: String): Result<Translation> {
        val queries = mapOf(
            "client" to "gtx",
            "sl" to from,
            "tl" to to,
            "dt" to "t",
            "dj" to "1",
            "q" to text
        )

        return Http.get(URL, queries = queries) {
            if (this.isOk) {
                try {
                    val translation = this.asJson<Translation>(GSON)
                    if (translation.src == from) {
                        Result.failure(SameLanguageError(from))
                    } else {
                        Result.success(translation)
                    }
                } catch (e: Exception) {
                    Result.failure(TranslationError("UnknownError", e.message ?: "Unknown error"))
                }
            } else {
                Result.failure(TranslationError(this.status, this.asText()))
            }
        }
    }
}
