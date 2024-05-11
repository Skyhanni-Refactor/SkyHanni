package at.hannibal2.skyhanni.utils.http

import at.hannibal2.skyhanni.utils.types.Either
import com.google.gson.Gson

object GoogleTranslator {

    private const val URL = "https://translate.google.com/translate_a/single"
    private val GSON = Gson()

    data class Sentence(val text: String, val from: String, val to: String)
    data class Translation(val sentences: List<Sentence>, val src: String) {

        val text: String get() = sentences.joinToString(" ") { it.text }
    }

    open class Error(val error: String, val message: String) {
        override fun toString(): String = "$error: $message"
    }

    class SameLanguageError(val lang: String) :
        Error("SameLanguage", "The source and target languages are the same ($lang)")

    fun translate(text: String, from: String, to: String): Either<Error, Translation> {
        val queries = mapOf(
            "client" to "gtx",
            "sl" to from,
            "tl" to to,
            "dt" to "t",
            "dj" to "1",
            "q" to text
        )

        return Http.get(URL, queries = queries) { response ->
            if (response.isOk) {
                try {
                    val translation = response.asJson<Translation>(GSON)
                    if (translation.src == from) {
                        Either.Left(SameLanguageError(from))
                    } else {
                        Either.Right(translation)
                    }
                } catch (e: Exception) {
                    Either.Left(Error("UnknownError", e.message ?: "Unknown error"))
                }
            } else {
                Either.Left(Error(response.status, response.asText()))
            }
        }
    }
}
