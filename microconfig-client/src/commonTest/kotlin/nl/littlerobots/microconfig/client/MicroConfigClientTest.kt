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
package nl.littlerobots.microconfig.client

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.core.toByteArray
import io.ktor.utils.io.core.writeFully
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray

expect fun createTempFile(): Path

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION) expect annotation class IgnoreNative()

class MicroConfigClientTest {
  @Test
  @IgnoreNative
  fun `Gets config from the network and caches it`() {
    val content =
        "{\"settings\":{\"enableFeature\":true},\"overrides\":[{\"matches\":[{\"version\":\"<1.0.0\"}],\"settings\":{\"enableFeature\":false,\"obsoleteFeature\":false}}]}"
    val mockEngine = MockEngine { _ ->
      respond(
          content = content,
          status = HttpStatusCode.OK,
          headers = headersOf(HttpHeaders.ContentType, "application/json"))
    }

    val tmpFile = createTempFile()
    val configClient = MicroConfigClient(tmpFile.toString(), "/config.json", mockEngine, null)
    val result = runBlocking { configClient.getConfig() }

    val cached =
        SystemFileSystem.source(tmpFile).buffered().use { it.readByteArray().decodeToString() }

    assertTrue(result is ConfigResult.Network)
    assertEquals(content, cached)
  }

  @Test
  @IgnoreNative
  fun `Gets cached config on non 200 status code`() {
    val content =
        "{\"settings\":{\"enableFeature\":true},\"overrides\":[{\"matches\":[{\"version\":\"<1.0.0\"}],\"settings\":{\"enableFeature\":false,\"obsoleteFeature\":false}}]}"
    val mockEngine = MockEngine { _ ->
      respond(
          content = "oops",
          status = HttpStatusCode.ServiceUnavailable,
          headers = headersOf(HttpHeaders.ContentType, "application/json"))
    }

    val tmpFile = createTempFile()
    SystemFileSystem.sink(tmpFile).buffered().use { it.writeFully(content.toByteArray()) }
    val configClient = MicroConfigClient(tmpFile.toString(), "/config.json", mockEngine, null)
    val result = runBlocking { configClient.getConfig() }

    assertTrue(result is ConfigResult.Cache)
  }

  @Test
  @IgnoreNative
  fun `Returns unavailable if config is not cached`() {
    val mockEngine = MockEngine { _ ->
      respond(
          content = "oops",
          status = HttpStatusCode.ServiceUnavailable,
          headers = headersOf(HttpHeaders.ContentType, "application/json"))
    }
    val tmpFile = createTempFile()
    val configClient = MicroConfigClient(tmpFile.toString(), "/config.json", mockEngine, null)
    val result = runBlocking { configClient.getConfig() }

    assertTrue(result is ConfigResult.Unavailable)
  }

  @Test
  @IgnoreNative
  fun `Gets config from the network once per session`() {
    val content =
        "{\"settings\":{\"enableFeature\":true},\"overrides\":[{\"matches\":[{\"version\":\"<1.0.0\"}],\"settings\":{\"enableFeature\":false,\"obsoleteFeature\":false}}]}"
    val mockEngine = MockEngine { _ ->
      respond(
          content = content,
          status = HttpStatusCode.OK,
          headers =
              headersOf(
                  HttpHeaders.ContentType,
                  "application/json",
              ))
    }

    val tmpFile = createTempFile()
    val configClient = MicroConfigClient(tmpFile.toString(), "/config.json", mockEngine, null)
    runBlocking { configClient.getConfig() }
    val response = runBlocking { configClient.getConfig() }

    assertTrue(response is ConfigResult.Cache)
    assertEquals(1, mockEngine.requestHistory.size)
  }

  @Test
  @IgnoreNative
  fun `Returns unavailable on parse error`() {
    val content = """{"test"}"""

    val mockEngine = MockEngine { _ ->
      respond(
          content = content,
          status = HttpStatusCode.OK,
          headers =
              headersOf(
                  HttpHeaders.ContentType,
                  "application/json",
              ))
    }

    val tmpFile = createTempFile()
    val configClient = MicroConfigClient(tmpFile.toString(), "/config.json", mockEngine, null)
    val result = runBlocking { configClient.getConfig() }

    assertTrue(result is ConfigResult.Unavailable)
  }
}
