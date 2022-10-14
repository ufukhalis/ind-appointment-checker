package io.github.ufukhalis.ind

import io.github.ufukhalis.ind.config.appConfigModule
import io.github.ufukhalis.ind.model.IndLocation
import io.github.ufukhalis.ind.model.IndProduct
import io.github.ufukhalis.ind.model.productLocationCheck
import io.github.ufukhalis.ind.service.IndService
import io.github.ufukhalis.ind.service.indServiceModule
import io.github.ufukhalis.ind.service.messageServiceModule
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.inject
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

fun main(args: Array<String>) {

    val parser = ArgParser(programName = "IND Appointment Checker made by Ufuk Halis")
    val messagingType by parser.option(
        ArgType.String,
        shortName = "t",
        description = "Messaging Type(whatsApp, telegram)"
    ).required()

    val whatsAppApiKey by parser.option(ArgType.String, shortName = "wp-key", description = "WhatsApp Api Key")
    val whatsAppPhoneNumber by parser.option(ArgType.String, shortName = "wp-pn", description = "WhatsApp Phone Number")

    val telegramUserName by parser.option(ArgType.String, shortName = "tl-username", description = "Telegram User Name")

    val filterDate by parser.option(ArgType.String, shortName = "fd", description = "Filter date").required()
    val indLocationString by parser.option(
        ArgType.String,
        shortName = "l",
        description = "IND Locations (${IndLocation.values().joinToString()})"
    ).required()
    val indProductString by parser.option(
        ArgType.String,
        shortName = "pd",
        description = "IND appointment types (${IndProduct.values().joinToString()})"
    ).required()
    val period by parser.option(ArgType.Int, shortName = "p", description = "Checking period in seconds").default(30)

    parser.parse(args)

    val indProduct = IndProduct.of(indProductString)
    val indLocation = IndLocation.of(indLocationString)

    productLocationCheck(indProduct, indLocation)

    startKoin {
        modules(
            listOf(
                appConfigModule,
                indServiceModule,
                messageServiceModule
            )
        )
    }

    val indService: IndService by inject(IndService::class.java) {
        parametersOf(
            messagingType,
            LocalDate.parse(filterDate),
            indLocation,
            indProduct,
            whatsAppPhoneNumber,
            whatsAppApiKey,
            telegramUserName
        )
    }

    runBlocking {
        indService.start(period.seconds).join()
    }

}
