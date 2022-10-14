package io.github.ufukhalis.ind.service

import arrow.core.Either
import arrow.core.filterOrElse
import io.github.ufukhalis.ind.model.IndDataResponse
import io.github.ufukhalis.ind.model.IndLocation
import io.github.ufukhalis.ind.model.IndProduct
import io.github.ufukhalis.ind.model.IndResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.time.LocalDate
import kotlin.time.Duration

class IndService(
    private val httpClient: HttpClient,
    private val messageService: MessageService,
    private val filterDate: LocalDate,
    private val indLocation: IndLocation,
    private val indProduct: IndProduct
) {

    private val logger = KotlinLogging.logger {}

    private val url = "https://oap.ind.nl/oap/api/desks/${indLocation.value}/slots/?productKey=${indProduct.value}&persons=1"

    suspend fun start(interval: Duration): Job {
        return startPeriodicJob(interval) {
            indChecker()
        }
    }

    private suspend fun startPeriodicJob(
        interval: Duration,
        action: suspend CoroutineScope.() -> Either<Throwable, List<IndDataResponse>>
    ): Job {
        var isActive = true
        return CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {

                when (val result = action.invoke(this)) {
                    is Either.Right -> {
                        val message = buildMessage(result.value)

                        when(val messageResult = messageService.sendMessage(message)) {
                            is Either.Right -> {
                                logger.info { "Appointment found message has been sent to your phone!" }
                            }
                            is Either.Left -> {
                                logger.error { "Message couldn't sent, the result will be available here -> ${messageResult.value}" }
                                logger.info { message }
                            }
                        }

                        isActive = false

                        logger.info { "Appointments found, the application will exit." }
                    }
                    is Either.Left -> logger.error { result.value.message }
                }

                delay(interval)
            }
        }
    }

    private fun buildMessage(result: List<IndDataResponse>): String {
        return """
            Good news! Appointment found!
            Appointment Type -> ${indProduct.name}
            Location -> ${indLocation.name}
            Available Date -> ${result.first().date} ${result.first().startTime}
            Link -> https://ind.nl/en/service-contact/make-an-appointment-with-the-ind
            Be quick and do appointment as soon as possible.
        """.trimIndent()
    }

    private suspend fun indChecker(): Either<Throwable, List<IndDataResponse>> {
        val response = requestToInd()

        return response
            .map { it.data }
            .map {
                it.filter { list -> list.date.isBefore(filterDate) }
            }.filterOrElse(
                predicate = { it.isNotEmpty() },
                default = { RuntimeException("Appointment not found for less than this filter -> Less than $filterDate and ${indLocation.name}") }
            )
    }

    private suspend fun requestToInd(): Either<Throwable, IndResponse> {
        return runCatching {
            val response = httpClient.get(url).body<String>().replace(")]}',", "")
            Either.Right(Json.decodeFromString<IndResponse>(response))
        }.getOrElse { Either.Left(it) }
    }
}

val indServiceModule = module {
    single { params ->
        val httpClient: HttpClient by inject(named("httpClient"))

        val phoneNumber = params.get<String>(4)
        val apiKey = params.get<String>(5)
        val telegramUserName = params.get<String>(6)

        val messageService = when (params.get<String>(0)) {
            "telegram" -> inject<MessageService>(named("telegram")) { parametersOf(telegramUserName) }
            "whatsApp" -> inject<MessageService>(named("whatsApp")) { parametersOf(phoneNumber, apiKey) }
            else -> throw RuntimeException("Messaging service type is unknown!")
        }.value

        val filterDate = params.get<LocalDate>(1)
        val indLocation = params.get<IndLocation>(2)
        val indProduct = params.get<IndProduct>(3)

        IndService(httpClient, messageService, filterDate, indLocation, indProduct)
    }
}
