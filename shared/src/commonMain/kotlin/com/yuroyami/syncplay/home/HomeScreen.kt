package com.yuroyami.syncplay.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yuroyami.syncplay.navigation.JellyfinNavigation
import com.yuroyami.syncplay.navigation.AndroidJellyfinNavigation
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberJellyfinNavigation(): JellyfinNavigation {
    val context = LocalContext.current
    return remember(context) { AndroidJellyfinNavigation(context) }
}

@Composable
fun JellyfinButton(
    modifier: Modifier = Modifier,
    navigation: JellyfinNavigation = rememberJellyfinNavigation()
) {
    Button(
        border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary),
        modifier = modifier.height(54.dp).aspectRatio(1.6f),
        shape = RoundedCornerShape(25),
        onClick = { navigation.openJellyfinBrowser() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.LiveTv,
                contentDescription = "Jellyfin",
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Jellyfin",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

// ... rest of HomeScreen.kt content ...
