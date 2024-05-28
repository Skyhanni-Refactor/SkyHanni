package at.hannibal2.skyhanni.utils.http

import at.hannibal2.skyhanni.SkyHanniMod
import com.google.gson.Gson
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection

object Http {

    private fun String.urlEncode() = URLEncoder.encode(this, "UTF-8")

    private fun createUrl(url: String, queries: Map<String, Any> = mapOf()): URL {
        val query = queries.entries.joinToString("&") {
            "${it.key.urlEncode()}=${it.value.toString().urlEncode()}"
        }
        return URL("$url?$query")
    }

    private fun createConnection(
        url: String,
        timeout: Int = 10000,
        queries: Map<String, Any> = mapOf(),
        headers: Map<String, String> = mapOf()
    ): HttpsURLConnection {
        require(url.startsWith("https://")) { "Only HTTPS is supported" }
        val connection = createUrl(url, queries).openConnection() as HttpsURLConnection
        connection.connectTimeout = timeout
        connection.readTimeout = timeout
        connection.useCaches = true
        connection.addRequestProperty("User-Agent", "SkyHanni (${SkyHanniMod.version})")

        headers.forEach { (key, value) ->
            connection.addRequestProperty(key, value)
        }

        return connection
    }

    /**
     * This will perform a GET request on a provided url and return a response that is processed by a provided handler.
     *
     * @param url: The URL to send the GET request to
     * @param queries: The queries to append to the URL
     * @param timeout: The timeout in milliseconds
     * @param headers: The headers to send with the request
     * @param handler: The handler to process the response
     * @return: The data returned by the handler
     */
    suspend fun <T : Any> get(
        url: String,
        queries: Map<String, Any> = mapOf(),
        timeout: Int = 10000,
        headers: Map<String, String> = mapOf(),
        handler: suspend HttpResponse.() -> T
    ): T {
        val connection = createConnection(url, timeout, queries, headers)
        connection.requestMethod = "GET"

        val data = handler(HttpResponse(connection.responseCode, connection.headerFields, connection.inputStream))

        connection.disconnect()

        return data
    }

    /**
     * This will perform a GET request on a provided url and return data as a Result.
     *
     * @param url: The URL to send the GET request to
     * @param gson: The Gson instance to parse the response
     * @param errorFactory: The factory to create an error from the response if it is not successful
     * @param queries: The queries to append to the URL
     * @param timeout: The timeout in milliseconds
     * @param headers: The headers to send with the request
     * @return: The data returned by the handler
     */
    suspend inline fun <reified T : Any> getResult(
        url: String,
        gson: Gson,
        crossinline errorFactory: (String) -> Exception,
        queries: Map<String, Any> = mapOf(),
        timeout: Int = 10000,
        headers: Map<String, String> = mapOf(),
    ): Result<T> {
        return try {
            get(url = url, queries = queries, timeout = timeout, headers = headers) {
                if (isOk) {
                    Result.success(asJson<T>(gson))
                } else {
                    Result.failure(errorFactory(asText()))
                }
            }
        } catch (e: Exception) {
            Result.failure(errorFactory(e.message ?: "Unknown error"))
        }
    }

    /**
     * This will perform a POST request on a provided url and return a response that is processed by a provided handler.
     *
     * @param url: The URL to send the POST request to
     * @param timeout: The timeout in milliseconds
     * @param queries: The queries to append to the URL
     * @param headers: The headers to send with the request
     * @param body: The body to send with the request
     * @param handler: The handler to process the response
     */
    suspend fun <T : Any> post(
        url: String,
        timeout: Int = 10000,
        queries: Map<String, Any> = mapOf(),
        headers: Map<String, String> = mapOf(),
        body: String,
        handler: suspend HttpResponse.() -> T
    ): T {
        val connection = createConnection(url, timeout, queries, headers)
        connection.requestMethod = "POST"
        connection.doOutput = true

        connection.outputStream.use { it.write(body.toByteArray()) }

        val data = handler(HttpResponse(connection.responseCode, connection.headerFields, connection.inputStream))

        connection.disconnect()

        return data
    }

    /**
     * This will perform a POST request on a provided url and return a response that is processed by a provided handler.
     *
     * @param url: The URL to send the POST request to
     * @param timeout: The timeout in milliseconds
     * @param queries: The queries to append to the URL
     * @param headers: The headers to send with the request
     * @param gson: The Gson instance to parse the response
     * @param body: The body to send with the request
     * @param handler: The handler to process the response
     * @return: The data returned by the handler
     */
    suspend fun <T : Any> post(
        url: String,
        timeout: Int = 10000,
        queries: Map<String, Any> = mapOf(),
        headers: Map<String, String> = mapOf(),
        gson: Gson,
        body: Any,
        handler: suspend HttpResponse.() -> T
    ): T {
        val newHeaders = headers.toMutableMap()
        newHeaders["Content-Type"] = "application/json"
        return post(url, timeout, queries, newHeaders, gson.toJson(body), handler)
    }
}
