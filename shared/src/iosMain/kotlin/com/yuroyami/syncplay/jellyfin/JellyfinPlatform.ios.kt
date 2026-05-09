package com.yuroyami.syncplay.jellyfin

import androidx.compose.runtime.Composable
import com.yuroyami.syncplay.navigation.JellyfinNavigation

actual fun createJellyfinRepository(): JellyfinRepository = object : JellyfinRepository {
    override suspend fun authenticate(serverUrl: String, username: String, password: String): Result<JellyfinConfig> {
        return Result.failure(UnsupportedOperationException("Jellyfin is only implemented on Android"))
    }

    override suspend fun getLibraries(): Result<List<JellyfinMediaItem>> {
        return Result.failure(UnsupportedOperationException("Jellyfin is only implemented on Android"))
    }

    override suspend fun getMediaItems(parentId: String): Result<List<JellyfinMediaItem>> {
        return Result.failure(UnsupportedOperationException("Jellyfin is only implemented on Android"))
    }

    override suspend fun getStreamUrl(itemId: String): Result<String> {
        return Result.failure(UnsupportedOperationException("Jellyfin is only implemented on Android"))
    }
}

@Composable
actual fun rememberJellyfinNavigation(): JellyfinNavigation? = null
