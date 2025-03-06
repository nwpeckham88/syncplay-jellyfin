package com.yuroyami.syncplay.jellyfin

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class AuthenticationResult(
    val AccessToken: String,
    val ServerId: String,
    val UserId: String
)

@Serializable
private data class JellyfinLibrary(
    val Id: String,
    val Name: String,
    val Type: String,
    val ImageTags: Map<String, String>
)

@Serializable
private data class JellyfinItem(
    val Id: String,
    val Name: String,
    val Type: String,
    val Overview: String? = null,
    val SeriesName: String? = null,
    val ParentIndexNumber: Int? = null, // Season number
    val IndexNumber: Int? = null, // Episode number
    val ImageTags: Map<String, String> = emptyMap()
)

class JellyfinRepositoryImpl : JellyfinRepository {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        
        install(Auth)
        
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 60000
        }
        
        install(DefaultRequest) {
            contentType(ContentType.Application.Json)
        }
        
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    private var config: JellyfinConfig? = null

    override suspend fun authenticate(
        serverUrl: String,
        username: String,
        password: String
    ): Result<JellyfinConfig> = JellyfinDebug.measureCall("/Users/AuthenticateByName") {
        try {
            val finalServerUrl = serverUrl.removeSuffix("/")
            val response = client.post("$finalServerUrl/Users/AuthenticateByName") {
                contentType(ContentType.Application.Json)
                setBody("""{"Username":"$username","Pw":"$password"}""")
            }
            
            val result = response.body<AuthenticationResult>()
            val newConfig = JellyfinConfig(
                serverUrl = finalServerUrl,
                apiKey = result.AccessToken,
                userId = result.UserId
            )
            config = newConfig

            client.plugin(Auth).bearer {
                loadTokens {
                    BearerTokens(result.AccessToken, "")
                }
            }

            Result.success(newConfig)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to authenticate: ${e.message}", e))
        }
    } as Result<JellyfinConfig>

    override suspend fun getLibraries(): Result<List<JellyfinMediaItem>> = 
        JellyfinDebug.measureCall("/Users/${config?.userId}/Views") {
            try {
                val currentConfig = requireConfig()
                
                val response = client.get("${currentConfig.serverUrl}/Users/${currentConfig.userId}/Views")
                val libraries = response.body<List<JellyfinLibrary>>()
                
                Result.success(libraries.map { library ->
                    JellyfinMediaItem(
                        id = library.Id,
                        name = library.Name,
                        type = library.Type,
                        overview = "",
                        imageUrl = if (library.ImageTags.containsKey("Primary")) {
                            "${currentConfig.serverUrl}/Items/${library.Id}/Images/Primary?api_key=${currentConfig.apiKey}"
                        } else null
                    )
                })
            } catch (e: Exception) {
                Result.failure(Exception("Failed to load libraries: ${e.message}", e))
            }
        } as Result<List<JellyfinMediaItem>>

    override suspend fun getMediaItems(parentId: String): Result<List<JellyfinMediaItem>> = 
        JellyfinDebug.measureCall("/Users/${config?.userId}/Items") {
            try {
                val currentConfig = requireConfig()
                
                val response = client.get("${currentConfig.serverUrl}/Users/${currentConfig.userId}/Items") {
                    url {
                        parameters.append("ParentId", parentId)
                        parameters.append("SortBy", "SortName")
                        parameters.append("SortOrder", "Ascending")
                        parameters.append("api_key", currentConfig.apiKey)
                    }
                }
                
                val items = response.body<List<JellyfinItem>>()
                Result.success(items.map { item ->
                    JellyfinMediaItem(
                        id = item.Id,
                        name = item.Name,
                        type = item.Type,
                        overview = item.Overview ?: "",
                        seriesName = item.SeriesName,
                        seasonNumber = item.ParentIndexNumber,
                        episodeNumber = item.IndexNumber,
                        imageUrl = if (item.ImageTags.containsKey("Primary")) {
                            "${currentConfig.serverUrl}/Items/${item.Id}/Images/Primary?api_key=${currentConfig.apiKey}"
                        } else null
                    )
                })
            } catch (e: Exception) {
                Result.failure(Exception("Failed to load media items: ${e.message}", e))
            }
        } as Result<List<JellyfinMediaItem>>

    override suspend fun getStreamUrl(itemId: String): Result<String> = 
        JellyfinDebug.measureCall("/Videos/$itemId/stream") {
            try {
                val currentConfig = requireConfig()
                Result.success("${currentConfig.serverUrl}/Videos/$itemId/stream?static=true&api_key=${currentConfig.apiKey}")
            } catch (e: Exception) {
                Result.failure(Exception("Failed to get stream URL: ${e.message}", e))
            }
        } as Result<String>

    private fun requireConfig(): JellyfinConfig {
        return checkNotNull(config) { "Must authenticate before making API calls" }
    }

    override fun close() {
        client.close()
    }
}
