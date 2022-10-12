package io.github.ufukhalis.ind.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate

@Serializable
data class IndResponse(val status: String, val data: List<IndDataResponse>)

@Serializable
data class IndDataResponse(
    val key: String,
    @Serializable(DateSerializer::class)
    val date: LocalDate,
    val startTime: String, val endTime: String, val parts: Int
)

object DateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("date", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: LocalDate) {
        TODO("Not yet implemented")
    }
}

enum class IndLocation(val value: String) {
    AMSTERDAM("AM"),
    DEN_HAAG("DH"),
    RIJSWIJK_TEMP("e1afaa1ca15c1778e972efb79ce63633"),
    ZWOLLE("ZW"),
    DEN_BOSCH("DB"),
    HAARLEM("6b425ff9f87de136a36b813cccf26e23"),
    EXPAT_CENTER_GRONINGEN("0c127eb6d9fe1ced413d2112305e75f6"),
    EXPAT_CENTER_MAASTRICHT("6c5280823686521552efe85094e607cf"),
    EXPAT_CENTER_WAGENINGEN("b084907207cfeea941cd9698821fd894"),
    EXPAT_CENTER_EINDHOVEN("0588ef4088c08f53294eb60bab55c81e"),
    EXPAT_CENTER_DEN_HAAG("5e325f444aeb56bb0270a61b4a0403eb"),
    EXPAT_CENTER_ROTTERDAM("f0ef3c8f0973875936329d713a68c5f3"),
    EXPAT_CENTER_ENSSCHEDE("3535aca0fb9a2e8e8015f768fb3fa69d"),
    EXPAT_CENTER_UTRECHT("fa24ccf0acbc76a7793765937eaee440"),
    EXPAT_CENTER_AMSTERDAM("284b189314071dcd571df5bb262a31db");

    companion object {
        fun of(value: String): IndLocation {
            return IndLocation.values()
                .find { it.name == value }
                ?: run { throw RuntimeException("Given value is not a valid location!") }
        }
    }
}

enum class IndProduct(val value: String) {
    RESIDENCE_DOCUMENT("DOC"),
    BIOMETRIC("BIO"),
    RESIDENCE_STICKER("VAA"),
    RETURN_VISA("TKV");

    companion object {
        fun of(value: String): IndProduct {
            return values()
                .find { it.name == value }
                ?: run { throw RuntimeException("Given value is not a valid appointment type!") }
        }
    }
}

private val productWithLocations = mapOf(
    IndProduct.RESIDENCE_DOCUMENT to listOf(
        IndLocation.AMSTERDAM,
        IndLocation.DEN_HAAG,
        IndLocation.RIJSWIJK_TEMP,
        IndLocation.ZWOLLE,
        IndLocation.DEN_BOSCH
    ),
    IndProduct.BIOMETRIC to listOf(
        IndLocation.AMSTERDAM,
        IndLocation.DEN_HAAG,
        IndLocation.ZWOLLE,
        IndLocation.DEN_BOSCH,
        IndLocation.HAARLEM,
        IndLocation.EXPAT_CENTER_GRONINGEN,
        IndLocation.EXPAT_CENTER_AMSTERDAM,
        IndLocation.EXPAT_CENTER_DEN_HAAG,
        IndLocation.EXPAT_CENTER_EINDHOVEN,
        IndLocation.EXPAT_CENTER_ENSSCHEDE,
        IndLocation.EXPAT_CENTER_MAASTRICHT,
        IndLocation.EXPAT_CENTER_ROTTERDAM,
        IndLocation.EXPAT_CENTER_WAGENINGEN,
        IndLocation.EXPAT_CENTER_UTRECHT
    ),
    IndProduct.RETURN_VISA to listOf(
        IndLocation.AMSTERDAM,
        IndLocation.DEN_HAAG,
        IndLocation.ZWOLLE,
        IndLocation.DEN_BOSCH
    ),
    IndProduct.RESIDENCE_STICKER to listOf(
        IndLocation.AMSTERDAM,
        IndLocation.DEN_HAAG,
        IndLocation.ZWOLLE,
        IndLocation.DEN_BOSCH
    )
)

fun productLocationCheck(product: IndProduct, location: IndLocation) {
    val locations = productWithLocations[product] ?: run {
        throw RuntimeException(
            "IND Appointment type is not valid! Valid values are ${IndProduct.values().joinToString()}"
        )
    }
    locations.find { it == location } ?: run {
        throw RuntimeException(
            "Given location is not correct for appointment type! Valid values are ${locations.joinToString()}"
        )
    }
}