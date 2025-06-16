package nl.littlerobots.microconfig.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.core.writeFully
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.serialization.json.Json
import nl.littlerobots.microconfig.Config

sealed class ConfigResult {
    data class Network(val config: Config) : ConfigResult()
    data class Cache(val config: Config) : ConfigResult()
    data object Unavailable : ConfigResult()
}

private val configJsonParser = Json {
    ignoreUnknownKeys = true
}

/**
 * Helper class to fetch and locally cache a config file from the network
 * By default this uses a [HttpClient] with the [HttpCache] installed and will
 * try to fetch the config from the memory http cache to reduce network calls if the
 * config is fetched often.
 *
 * @param cacheConfigPath the path to cache the config
 * @param httpClient the ktor http client to use.
 * @param logger an optional logger for logging error messages
 */
class MicroConfigClient(
    cacheConfigPath: String,
    private val configUrl: String,
    private val httpClient: HttpClient = HttpClient {
        install(HttpCache)
    },
    private val logger: Logger? = null
) {

    private val cacheConfigPath = Path(cacheConfigPath)

    interface Logger {
        fun log(message: String, exception: Throwable?)
    }

    /**
     * @param cacheConfigPath the path to cache the config
     * @param engine the ktor http client engine to use.
     * @param logger an optional logger for logging error messages
     */
    constructor(
        cacheConfigPath: String,
        configUrl: String,
        engine: HttpClientEngine,
        logger: Logger?
    ) : this(
        cacheConfigPath,
        configUrl,
        HttpClient(engine) { install(HttpCache) },
        logger
    )

    /**
     * Get the config from the server or cache. The client will attempt a network call with a
     * `max-age` `Cache-Control` header of [maxAgeHours].
     * If the network call fails the config will be returned from the locally cached, if available.
     *
     * @param maxAgeHours the number of hours the config may be stale when fetching from the network without refreshing
     * @return a [ConfigResult] that returns a (cached) config or [ConfigResult.Unavailable] if the config could not be retrieved.
     */
    suspend fun getConfig(maxAgeHours: Int = 4): ConfigResult {
        return runCatching {
            val response = httpClient.get(configUrl) {
                headers.append("Cache-Control", "max-stale=${maxAgeHours * 3600}")
            }
            if (response.status == HttpStatusCode.OK) {
                val configJson = response.bodyAsBytes()
                val config = configJsonParser.decodeFromString<Config>(configJson.decodeToString())
                if (response.headers["Warning"] == "110") {
                    ConfigResult.Cache(config)
                } else {
                    storeConfig(configJson)
                    ConfigResult.Network(config)
                }
            } else {
                getFromCache()?.let {
                    ConfigResult.Cache(it)
                } ?: ConfigResult.Unavailable
            }
        }.getOrElse {
            logger?.log("Error getting config from the network", it)
            getFromCache()?.let {
                ConfigResult.Cache(it)
            } ?: ConfigResult.Unavailable
        }
    }

    private suspend fun getFromCache(): Config? {
        return withContext(Dispatchers.IO) {
            if (SystemFileSystem.exists(cacheConfigPath)) {
                runCatching {
                    val configBytes = SystemFileSystem.source(cacheConfigPath).buffered().use {
                        it.readByteArray()
                    }
                    configJsonParser.decodeFromString<Config>(configBytes.decodeToString())
                }.getOrElse {
                    logger?.log("Could not get config from cache", it)
                    null
                }
            } else {
                null
            }
        }
    }

    private suspend fun storeConfig(config: ByteArray) {
        withContext(Dispatchers.IO) {
            runCatching {
                cacheConfigPath.parent?.let { SystemFileSystem.createDirectories(it) }
                val sink = SystemFileSystem.sink(cacheConfigPath)
                val buffer = Buffer().apply {
                    writeFully(config)
                }
                sink.write(buffer, buffer.size)
                sink.flush()
                sink.close()
            }.getOrElse {
                logger?.log("Error storing config", it)
            }
        }
    }
}