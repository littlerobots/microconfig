package nl.littlerobots.microconfig.client

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.core.toByteArray
import io.ktor.utils.io.core.writeFully
import kotlinx.coroutines.runBlocking
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

expect fun createTempFile(): Path

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
expect annotation class IgnoreNative()

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
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val tmpFile = createTempFile()
        val configClient = MicroConfigClient(tmpFile, mockEngine, null)
        val result = runBlocking {
            configClient.getConfig("/config.json")
        }

        val cached = SystemFileSystem.source(tmpFile).buffered().use {
            it.readByteArray().decodeToString()
        }

        assertTrue(result is ConfigResponse.Network)
        assertEquals(1, result.config.overrides.size)
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
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val tmpFile = createTempFile()
        SystemFileSystem.sink(tmpFile).buffered().use {
            it.writeFully(content.toByteArray())
        }
        val configClient = MicroConfigClient(tmpFile, mockEngine, null)
        val result = runBlocking {
            configClient.getConfig("/config.json")
        }

        assertTrue(result is ConfigResponse.Cache)
        assertEquals(1, result.config.overrides.size)
    }

    @Test
    @IgnoreNative
    fun `Returns unavailable if config is not cached`() {
        val mockEngine = MockEngine { _ ->
            respond(
                content = "oops",
                status = HttpStatusCode.ServiceUnavailable,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val tmpFile = createTempFile()
        val configClient = MicroConfigClient(tmpFile, mockEngine, null)
        val result = runBlocking {
            configClient.getConfig("/config.json")
        }

        assertTrue(result is ConfigResponse.Unavailable)
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
                headers = headersOf(
                    HttpHeaders.ContentType, "application/json",
                )
            )
        }

        val tmpFile = createTempFile()
        val configClient = MicroConfigClient(tmpFile, mockEngine, null)
        runBlocking {
            configClient.getConfig("/config.json")
        }
        val response = runBlocking {
            configClient.getConfig("/config.json")
        }

        assertTrue(response is ConfigResponse.Cache)
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
                headers = headersOf(
                    HttpHeaders.ContentType, "application/json",
                )
            )
        }

        val tmpFile = createTempFile()
        val configClient = MicroConfigClient(tmpFile, mockEngine, null)
        val result = runBlocking {
            configClient.getConfig("/config.json")
        }

        assertTrue(result is ConfigResponse.Unavailable)
    }
}