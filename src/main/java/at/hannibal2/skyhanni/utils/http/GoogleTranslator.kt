package at.hannibal2.skyhanni.utils.http

import at.hannibal2.skyhanni.utils.types.Either
import com.google.gson.Gson

object GoogleTranslator {

    private const val URL = "https://translate.google.com/translate_a/single"
    private val GSON = Gson()

    data class Sentence(val trans: String, val orig: String)
    data class Translation(val sentences: List<Sentence>, val src: String) {

        val text: String get() = sentences.joinToString(" ") { it.trans }
    }

    open class Error(val error: String, val message: String) {
        override fun toString(): String = "$error: $message"
    }

    class SameLanguageError(val lang: String) :
        Error("SameLanguage", "The source and target languages are the same ($lang)")

    suspend fun translate(text: String, from: String, to: String): Either<Error, Translation> {
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
                        Either.Left(SameLanguageError(from))
                    } else {
                        Either.Right(translation)
                    }
                } catch (e: Exception) {
                    Either.Left(Error("UnknownError", e.message ?: "Unknown error"))
                }
            } else {
                Either.Left(Error(this.status, this.asText()))
            }
        }
    }
}
