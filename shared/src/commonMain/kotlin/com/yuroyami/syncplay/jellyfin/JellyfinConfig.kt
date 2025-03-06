package com.yuroyami.syncplay.jellyfin

data class JellyfinConfig(
    val serverUrl: String = "",
    val apiKey: String = "",
    val userId: String = ""
)

// Represents a media item from Jellyfin
data class JellyfinMediaItem(
    val id: String,
    val name: String,
    val type: String, // "Movie", "Episode", etc.
    val overview: String,
    val seriesName: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val streamUrl: String? = null,
    val imageUrl: String? = null
)

// Repository interface for Jellyfin operations
interface JellyfinRepository {
    suspend fun authenticate(serverUrl: String, username: String, password: String): Result<JellyfinConfig>
    suspend fun getLibraries(): Result<List<JellyfinMediaItem>>
    suspend fun getMediaItems(parentId: String): Result<List<JellyfinMediaItem>>
    suspend fun getStreamUrl(itemId: String): Result<String>
}
