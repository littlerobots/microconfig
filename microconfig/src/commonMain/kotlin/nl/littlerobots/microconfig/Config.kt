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

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

@Serializable data class Config(val settings: JsonObject, val overrides: List<Override>)

typealias AppProperties = Map<String, RuntimeProperty>

private val resolveSettingsParser = Json {
  ignoreUnknownKeys = true
  isLenient = true
}

private fun matchesAny(
    conditions: List<ConditionProperties>,
    properties: Map<String, RuntimeProperty>
): Boolean {
  val keys = properties.keys
  return conditions.any { condition ->
    keys.containsAll(condition.keys) &&
        properties
            .filter { condition.keys.contains(it.key) }
            .all { it.value.matches(requireNotNull(condition[it.key])) }
  }
}

/**
 * Resolve the config and return a deserialized instance using the provided serializer
 *
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
    properties: AppProperties,
    activationTime: Instant = Clock.System.now()
): T {

  val resolvedSettings =
      overrides.fold(settings) { settings, override ->
        // if we have any unknown properties, it's no match
        if (matchesAny(override.matching, properties) &&
            override.schedule.matches(activationTime)) {
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
