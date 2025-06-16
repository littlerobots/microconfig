package nl.littlerobots.microconfig

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class Config(
    internal val settings: JsonObject,
    internal val overrides: List<Override>
)

private val resolveSettingsParser = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

private typealias ConditionProperties = Map<String, CoercedString>
private typealias CoercedString = @Serializable(with = CoercedStringSerializer::class) String

object CoercedStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("CoercedString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        val jsonElement = decoder.decodeSerializableValue(JsonElement.serializer())
        return if (jsonElement is JsonNull ||
            jsonElement is JsonObject ||
            jsonElement is JsonArray
        ) {
            ""
        } else {
            jsonElement.jsonPrimitive.content
        }
    }

    override fun serialize(encoder: Encoder, value: String) {
        error("Not implemented")
    }
}

@Serializable
data class Override(
    val settings: JsonObject = buildJsonObject {},
    val matching: List<ConditionProperties> = emptyList(),
    val schedule: Schedule? = null
)

@Serializable
data class Schedule(val from: Instant? = null, val until: Instant? = null)

private fun matchesAny(
    conditions: List<ConditionProperties>,
    properties: Map<String, RuntimeProperty>
): Boolean {
    val keys = properties.keys
    return conditions.any { condition ->
        keys.containsAll(condition.keys) &&
            properties.filter { condition.keys.contains(it.key) }
                .all {
                    it.value.matches(
                        requireNotNull(
                            condition[it.key]
                        )
                    )
                }
    }
}

/**
 * Resolve the config and return a deserialized instance using the provided serializer
 * @param serializer the serializer to serializing the settings to an object
 * @param properties the current values for the properties used to match any override present
 * @param activationTime the time of resolving, used evaluate a schedule for a config override
 * @return the serialized settings
 * @throws SerializationException if serialization fails
 * @throws IllegalArgumentException if serialization fails
 */
@Throws(SerializationException::class, IllegalArgumentException::class)
fun <T> Config.resolve(
    serializer: KSerializer<T>,
    properties: Map<String, RuntimeProperty>,
    activationTime: Instant = Clock.System.now()
): T {

    val resolvedSettings = overrides.fold(settings) { settings, override ->
        // if we have any unknown properties, it's no match
        if (matchesAny(
                override.matching,
                properties
            ) && override.schedule.matches(activationTime)
        ) {
            buildJsonObject {
                for (entry in settings) {
                    put(entry.key, entry.value)
                }
                for (entry in override.settings) {
                    put(entry.key, entry.value)
                }
            }
        } else {
            settings
        }
    }
    JsonElement.serializer()
    return resolveSettingsParser.decodeFromJsonElement(serializer, resolvedSettings)
}

private fun Schedule?.matches(instant: Instant): Boolean {
    if (this == null) {
        return true
    }
    if (from != null && instant < from) {
        return false
    }
    if (until != null && instant >= until) {
        return false
    }
    if (from == null || until == null) {
        return true
    }
    return instant >= from && instant < until
}