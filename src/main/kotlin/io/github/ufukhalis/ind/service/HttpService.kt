package io.github.ufukhalis.ind.service

import arrow.core.Either
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import mu.KotlinLogging

interface HttpService {

    suspend fun sendRequest(httpClient: HttpClient, url: String, errorMessage: String): Either<Throwable, Int> {
        return runCatching {
            val httpResponse = httpClient.get(url)

            when (val code = httpResponse.status.value) {
                in 200..299 -> Either.Right(code)
                else -> Either.Left(RuntimeException("$errorMessage -> $code"))
            }
        }.getOrElse {
            Either.Left(it)
        }
    }

    suspend fun sendRequestWithRetry(httpClient: HttpClient, url: String, errorMessage: String) {
        retry {
            sendRequest(httpClient, url, errorMessage)
        }
    }
}

private val logger = KotlinLogging.logger {}

suspend fun <R> retry(maxRetryCount: Int = 3,
                      initialDelay: Long = 100,
                      maxDelay: Long = 1000,
                      factor: Double = 2.0,
                      block: suspend () -> Either<Throwable, R>): Either<Throwable, R> {
    var currentDelay = initialDelay
    repeat(times = maxRetryCount) {
        when(val result = block.invoke()) {
            is Either.Right -> Either.Right(result.value)
            is Either.Left -> {
                logger.warn { "There is a problem with http request, it will be retried, remaining retry count ${maxRetryCount - it -1}..." }
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            }
        }
    }
    return block.invoke()
}