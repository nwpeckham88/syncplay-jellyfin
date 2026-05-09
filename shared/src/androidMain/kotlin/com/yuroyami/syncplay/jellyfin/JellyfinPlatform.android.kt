package com.yuroyami.syncplay.jellyfin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.yuroyami.syncplay.navigation.AndroidJellyfinNavigation
import com.yuroyami.syncplay.navigation.JellyfinNavigation

actual fun createJellyfinRepository(): JellyfinRepository = JellyfinRepositoryImpl()

@Composable
actual fun rememberJellyfinNavigation(): JellyfinNavigation? {
    val context = LocalContext.current
    return remember(context) { AndroidJellyfinNavigation(context) }
}
