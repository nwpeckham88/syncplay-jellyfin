package com.yuroyami.syncplay.jellyfin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.lyricist.Lyricist
import com.yuroyami.syncplay.lyricist.Stringies
import com.yuroyami.syncplay.models.JoinInfo
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

/**
 * Main container composable that handles the navigation between Jellyfin screens
 * and provides the ViewModel to child composables.
 */
@Composable
fun JellyfinContainer(
    onJoinRoom: (JoinInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = remember { 
        JellyfinViewModel(
            repository = JellyfinRepositoryImpl(),
            onJoinRoom = onJoinRoom
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (viewModel.uiState) {
            JellyfinUiState.Login -> JellyfinLoginScreen(viewModel = viewModel)
            JellyfinUiState.Browser -> JellyfinBrowserScreen(viewModel = viewModel)
        }

        viewModel.errorMessage?.let { error ->
            ErrorSnackbar(
                message = error,
                onDismiss = viewModel::clearError
            )
        }
    }
}

/**
 * A reusable composable for displaying Jellyfin media images with loading and error states
 */
@Composable
fun JellyfinImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 2f / 3f // Default movie poster ratio
) {
    if (url != null) {
        KamelImage(
            resource = asyncPainterResource(url),
            contentDescription = contentDescription,
            modifier = modifier.aspectRatio(aspectRatio),
            onLoading = { progress -> 
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            },
            onFailure = { exception ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Failed to load image")
                }
            }
        )
    } else {
        Box(
            modifier = modifier.aspectRatio(aspectRatio),
            contentAlignment = Alignment.Center
        ) {
            Text("No image")
        }
    }
}
