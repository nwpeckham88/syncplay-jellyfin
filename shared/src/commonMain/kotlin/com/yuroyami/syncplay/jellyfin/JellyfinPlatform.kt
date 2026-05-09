package com.yuroyami.syncplay.jellyfin

import androidx.compose.runtime.Composable
import com.yuroyami.syncplay.navigation.JellyfinNavigation

expect fun createJellyfinRepository(): JellyfinRepository

@Composable
expect fun rememberJellyfinNavigation(): JellyfinNavigation?
