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

}

class WhatsAppMessageService(
    private val httpClient: HttpClient,
    private val phoneNumber: String,
    private val apiKey: String
) : MessageService {

    private val baseUrl = "https://api.callmebot.com/whatsapp.php?"

    override suspend fun sendMessage(message: String): Either<Throwable, Int> {
        return runCatching {
            val encoded = withContext(Dispatchers.IO) {
                URLEncoder.encode(message, "UTF-8")
            }
            val response = httpClient.get("${baseUrl}phone=${phoneNumber}&text=$encoded&apikey=${apiKey}")

            when (val value = response.status.value) {
                in 200..299 -> Either.Right(value)
                else -> Either.Left(RuntimeException("The WhatsApp API has some issues -> $value"))
            }

        }.getOrElse {
            Either.Left(it)
        }
    }
}

class TelegramMessageService : MessageService {
    override suspend fun sendMessage(message: String): Either<Throwable, Int> {
        TODO("Not yet implemented")
    }
}

val messageServiceModule = module {
    single<MessageService>(named("whatsApp")) { params ->

        val httpClient: HttpClient by inject(named("httpClient"))

        WhatsAppMessageService(httpClient, params[0], params[1])
    }

    single<MessageService>(named("telegram")) {
        TelegramMessageService()
    }
}