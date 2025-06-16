/*
 * Copyright 2025 Little Robots
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.littlerobots.microconfig

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive

internal typealias ConditionProperties = Map<String, CoercedString>

private typealias CoercedString = @Serializable(with = CoercedStringSerializer::class) String

object CoercedStringSerializer : KSerializer<String> {
  override val descriptor: SerialDescriptor
    get() =
        PrimitiveSerialDescriptor("nl.littlerobots.microconfig.CoercedString", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): String {
    val jsonElement = decoder.decodeSerializableValue(JsonElement.serializer())
    return if (jsonElement is JsonNull || jsonElement is JsonObject || jsonElement is JsonArray) {
      ""
    } else {
      jsonElement.jsonPrimitive.content
    }
  }

  override fun serialize(encoder: Encoder, value: String) {
    encoder.encodeString(value)
  }
}

@Serializable
data class Override(
    val settings: JsonObject = buildJsonObject {},
    val matching: List<ConditionProperties> = emptyList(),
    val schedule: Schedule? = null
)
