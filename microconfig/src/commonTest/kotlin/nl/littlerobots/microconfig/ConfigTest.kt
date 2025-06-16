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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ConfigTest {
  private val parser = Json { ignoreUnknownKeys = true }

  @Serializable
  data class TestSettings(
      val enableFeature: Boolean = false,
      val obsoleteFeature: String = "disabled",
      val appstate: AppState = AppState.ACTIVE
  ) {
    @Serializable
    enum class AppState {
      @SerialName("active") ACTIVE,
      @SerialName("inactive") INACTIVE
    }
  }

  @Test
  fun `Resolves default config`() {
    val json =
        """
            {
              "settings": {
                "enableFeature": true
              },
              "overrides": [
                {
                  "matching": [{
                    "version": "<1.0.0"
                  }],
                  "settings": {
                    "enableFeature": false,
                    "obsoleteFeature": "disabled"                   
                  }
                }
              ]
            }
        """
            .trimIndent()
    val config = parser.decodeFromString<Config>(json)
    val settings = config.resolve(TestSettings.serializer(), emptyMap())
    assertEquals(TestSettings(enableFeature = true, obsoleteFeature = "disabled"), settings)
  }

  @Test
  fun `Resolves override`() {
    val json =
        """
            {
              "settings": {
                "enableFeature": true
              },
              "overrides": [
                {
                  "matching": [{
                    "version": "<1.0.0"
                  }],
                  "settings": {
                    "enableFeature": false,
                    "obsoleteFeature": "enabled",
                    "appstate" : "inactive"
                  }
                }
              ]
            }
        """
            .trimIndent()
    val config = parser.decodeFromString<Config>(json)
    val settings =
        config.resolve(
            TestSettings.serializer(),
            propertiesOf(
                "version" to VersionProperty("0.8.2", { fail("Should handle constraint") })))
    assertEquals(
        TestSettings(
            enableFeature = false,
            obsoleteFeature = "enabled",
            appstate = TestSettings.AppState.INACTIVE),
        settings)
  }

  @Test
  fun `Does not resolve override for missing properties`() {
    val json =
        """
            {
              "settings": {
                "enableFeature": true
              },
              "overrides": [
                {
                  "matching": [{
                    "version": "<1.0.0",
                    "platform" : "windows"
                  }],
                  "settings": {
                    "enableFeature": false,
                    "obsoleteFeature": "enabled",
                    "appstate" : "inactive"
                  }
                }
              ]
            }
        """
            .trimIndent()
    val config = parser.decodeFromString<Config>(json)
    val settings =
        config.resolve(
            TestSettings.serializer(),
            propertiesOf(
                "version" to VersionProperty("0.8.2", { fail("Should handle constraint") })))
    assertEquals(TestSettings(enableFeature = true), settings)
  }
}
