package com.yuroyami.syncplay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yuroyami.syncplay.jellyfin.JellyfinManualTest
import com.yuroyami.syncplay.ui.AppTheme
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Debug activity for testing Jellyfin integration with a real server.
 * Only available in debug builds.
 */
class JellyfinTestRunner : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var serverUrl by remember { mutableStateOf("http://localhost:8096") }
                    var username by remember { mutableStateOf("test") }
                    var password by remember { mutableStateOf("test123") }
                    var testOutput by remember { mutableStateOf("") }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Jellyfin Test Runner",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        TextField(
                            value = serverUrl,
                            onValueChange = { serverUrl = it },
                            label = { Text("Server URL") },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        TextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        TextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = {
                                updateTestConfig(serverUrl, username, password)
                                GlobalScope.launch(Dispatchers.IO) {
                                    testOutput = captureOutput {
                                        JellyfinManualTest.runAllTests()
                                    }
                                }
                            },
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Text("Run All Tests")
                        }

                        Button(
                            onClick = {
                                updateTestConfig(serverUrl, username, password)
                                GlobalScope.launch(Dispatchers.IO) {
                                    testOutput = captureOutput {
                                        JellyfinManualTest.testAuthentication()
                                    }
                                }
                            },
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Text("Test Authentication")
                        }

                        Button(
                            onClick = {
                                updateTestConfig(serverUrl, username, password)
                                GlobalScope.launch(Dispatchers.IO) {
                                    testOutput = captureOutput {
                                        JellyfinManualTest.testLibraryAccess()
                                    }
                                }
                            },
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Text("Test Library Access")
                        }

                        Text(
                            text = "Test Output:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(text = testOutput)
                    }
                }
            }
        }
    }

    private fun updateTestConfig(serverUrl: String, username: String, password: String) {
        // Update test config through reflection since it's private
        val testConfig = Class.forName("com.yuroyami.syncplay.jellyfin.JellyfinManualTest\$TEST_CONFIG")
        val fields = testConfig.declaredFields
        fields.forEach { field ->
            field.isAccessible = true
            when (field.name) {
                "SERVER_URL" -> field.set(null, serverUrl)
                "USERNAME" -> field.set(null, username)
                "PASSWORD" -> field.set(null, password)
            }
        }
    }

    private fun captureOutput(block: () -> Unit): String {
        val output = StringBuilder()
        val originalOut = System.out
        val outputStream = java.io.ByteArrayOutputStream()
        System.setOut(java.io.PrintStream(outputStream))
        
        try {
            block()
            output.append(outputStream.toString())
        } finally {
            System.setOut(originalOut)
        }
        
        return output.toString()
    }
}
