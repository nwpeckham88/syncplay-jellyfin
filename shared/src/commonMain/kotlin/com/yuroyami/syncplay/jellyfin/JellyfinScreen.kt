package com.yuroyami.syncplay.jellyfin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JellyfinLoginScreen(
    viewModel: JellyfinViewModel,
    modifier: Modifier = Modifier
) {
    var serverUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(viewModel.loginState) {
        if (viewModel.loginState is LoginState.Error) {
            // Show error in UI
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Connect to Jellyfin",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = serverUrl,
            onValueChange = { serverUrl = it },
            label = { Text("Server URL") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            singleLine = true
        )

        Button(
            onClick = { viewModel.login(serverUrl, username, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewModel.loginState != LoginState.Loading && 
                     serverUrl.isNotBlank() && 
                     username.isNotBlank() && 
                     password.isNotBlank()
        ) {
            if (viewModel.loginState == LoginState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Connect")
            }
        }

        (viewModel.loginState as? LoginState.Error)?.let { error ->
            Text(
                text = error.message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JellyfinBrowserScreen(
    viewModel: JellyfinViewModel,
    modifier: Modifier = Modifier
) {
    val selectedLibrary = viewModel.selectedLibrary
    val mediaItems = viewModel.mediaItems

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Libraries row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(viewModel.libraries.size) { index ->
                val library = viewModel.libraries[index]
                ElevatedFilterChip(
                    selected = library == selectedLibrary,
                    onClick = { viewModel.selectLibrary(library) },
                    label = { Text(library.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Media items grid
        if (viewModel.mediaItems.isEmpty() && selectedLibrary != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(mediaItems.size) { index ->
                    val item = mediaItems[index]
                    MediaItemCard(
                        item = item,
                        onClick = { viewModel.selectMedia(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaItemCard(
    item: JellyfinMediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            JellyfinImage(
                url = item.imageUrl,
                contentDescription = item.name
            )
            
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2
                )
                
                if (item.seriesName != null) {
                    Text(
                        text = "${item.seriesName} - S${item.seasonNumber}E${item.episodeNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorSnackbar(
    message: String,
    onDismiss: () -> Unit,
    duration: SnackbarDuration = SnackbarDuration.Short
) {
    Snackbar(
        modifier = Modifier.padding(16.dp),
        action = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        },
        dismissAction = onDismiss
    ) {
        Text(message)
    }
}
