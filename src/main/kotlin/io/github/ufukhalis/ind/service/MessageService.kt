package io.github.ufukhalis.ind.service

import arrow.core.Either
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.net.URLEncoder

interface MessageService {

    suspend fun sendMessage(message: String): Either<Throwable, Int>

    suspend fun encodeMessage(message: String): String {
        return withContext(Dispatchers.IO) {
            URLEncoder.encode(message, "UTF-8")
        }
    }
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
}

class WhatsAppMessageService(
    private val httpClient: HttpClient,
    private val phoneNumber: String,
    private val apiKey: String
) : MessageService {

    private val baseUrl = "https://api.callmebot.com/whatsapp.php?"

    override suspend fun sendMessage(message: String): Either<Throwable, Int> {
        val encodedMessage = encodeMessage(message)
        val url = "${baseUrl}phone=${phoneNumber}&text=$encodedMessage&apikey=${apiKey}"

        return sendRequest(httpClient, url, "The WhatsApp API has some issues")
    }
}

class TelegramMessageService(
    private val httpClient: HttpClient,
    private val telegramUserName: String
) : MessageService {

    private val baseUrl = "https://api.callmebot.com/text.php?"
    override suspend fun sendMessage(message: String): Either<Throwable, Int> {
        val encodedMessage = encodeMessage(message)

        val url = "${baseUrl}user=@${telegramUserName}&text=${encodedMessage}"

        return sendRequest(httpClient, url, "The Telegram API has some issues")
    }
}

val messageServiceModule = module {
    single<MessageService>(named("whatsApp")) { params ->

        val httpClient: HttpClient by inject(named("httpClient"))

        WhatsAppMessageService(httpClient, params[0], params[1])
    }

    single<MessageService>(named("telegram")) { params ->

        val httpClient: HttpClient by inject(named("httpClient"))

        TelegramMessageService(httpClient, params[0])
    }
}
