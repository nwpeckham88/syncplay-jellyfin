package com.yuroyami.syncplay.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yuroyami.syncplay.jellyfin.rememberJellyfinNavigation
import com.yuroyami.syncplay.models.JoinInfo
import com.yuroyami.syncplay.watchroom.homeCallback

/**
 * Minimal home screen entry point for joining Syncplay rooms directly and, on Android,
 * continuing into the Jellyfin browsing flow.
 *
 * The screen delegates room joins through the shared `homeCallback`, which is initialized by
 * the host platform before this composable is shown. Join submissions call `JoinInfo.remember()`
 * before `get()` so the current form values are persisted using the app's existing join flow.
 *
 * @param config saved join values used to prefill the form.
 * @param modifier optional layout modifier for the screen container.
 */
@Composable
fun HomeScreen(
    config: HomeConfig,
    modifier: Modifier = Modifier
) {
    var username by remember(config.savedUser) { mutableStateOf(config.savedUser) }
    var roomName by remember(config.savedRoom) { mutableStateOf(config.savedRoom) }
    var serverAddress by remember(config.savedIP) { mutableStateOf(config.savedIP) }
    var portText by remember(config.savedPort) { mutableStateOf(config.savedPort.toString()) }
    var password by remember(config.savedPassword) { mutableStateOf(config.savedPassword) }
    val jellyfinNavigation = rememberJellyfinNavigation()

    val port = portText.toIntOrNull()
    val canJoin = username.isNotBlank() && roomName.isNotBlank() && serverAddress.isNotBlank() && port != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Syncplay",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Join a room directly or continue through Jellyfin.",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = roomName,
            onValueChange = { roomName = it },
            label = { Text("Room") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = serverAddress,
            onValueChange = { serverAddress = it },
            label = { Text("Server") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = portText,
            onValueChange = { portText = it.filter(Char::isDigit) },
            label = { Text("Port") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    val resolvedPort = port ?: return@Button
                    homeCallback?.onJoin(
                        JoinInfo(
                            username = username.trim(),
                            roomname = roomName.trim(),
                            address = serverAddress.trim(),
                            port = resolvedPort,
                            password = password
                        ).remember().get()
                    )
                },
                enabled = canJoin,
                modifier = Modifier.weight(1f)
            ) {
                Text("Join Room")
            }

            jellyfinNavigation?.let { navigation ->
                OutlinedButton(
                    onClick = navigation::openJellyfinBrowser,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LiveTv,
                        contentDescription = "Jellyfin"
                    )
                    Text(
                        text = "Jellyfin",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}
