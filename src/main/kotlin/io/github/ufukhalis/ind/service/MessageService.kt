package io.github.ufukhalis.ind.service

import arrow.core.Either
import io.ktor.client.*
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.net.URLEncoder

interface MessageService : HttpService {
    suspend fun sendMessage(message: String): Either<Throwable, Int>

    suspend fun encodeMessage(message: String): String {
        return with(message) {
            URLEncoder.encode(this, "UTF-8")
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
