package com.addi.thirdparty

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.future.await
import org.slf4j.Logger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

typealias ErrorHandler = suspend (url: String, status: Int, body: String) -> Exception

@ExperimentalTime
open class BaseClient(
    protected val baseUrl: String,
    protected val logger: Logger,
    protected val errorHandler: ErrorHandler,
    protected val objectMapper: ObjectMapper = jacksonMapperBuilder()
        .addModule(JavaTimeModule())
        .build()
) {

    companion object {
        const val NOT_FOUND = 404
    }

    protected val client: HttpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NEVER)
        .connectTimeout(Duration.ofSeconds(20))
        .build()

    protected suspend inline fun <reified T> get(path: String): T {
        val url = "$baseUrl$path"
        val uri = URI.create(url)
        val request = HttpRequest.newBuilder()
            .timeout(Duration.ofSeconds(20))
            .GET()
            .uri(uri)
            .build()

        val response = measureTimedValue {
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).await()
        }
        val statusCode = response.value.statusCode()
        val body = response.value.body()

        logger.info("GET url='$url' status='$statusCode' took '${response.duration.inWholeMilliseconds}'ms", )

        if (isError(statusCode)) {
            throw errorHandler.invoke(url, statusCode, body)
        }

        return objectMapper.readValue(body, object : TypeReference<T>() {})
    }

    protected fun isError(status: Int): Boolean {
        if ((status / 100) % 2 != 0) return true
        return false
    }



}