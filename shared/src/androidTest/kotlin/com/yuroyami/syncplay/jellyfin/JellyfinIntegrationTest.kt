package com.yuroyami.syncplay.jellyfin

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.test.runTest

@RunWith(AndroidJUnit4::class)
class JellyfinIntegrationTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<JellyfinActivity>()

    @Test
    fun completeFlow_loginThroughMediaSelection() {
        // Start at login screen
        composeTestRule.onNodeWithText("Connect to Jellyfin").assertExists()
        
        // Fill in login form
        composeTestRule.onNodeWithText("Server URL")
            .performTextInput("http://test-jellyfin.local")
        composeTestRule.onNodeWithText("Username")
            .performTextInput("testuser")
        composeTestRule.onNodeWithText("Password")
            .performTextInput("password123")
        
        // Submit login
        composeTestRule.onNodeWithText("Connect").performClick()
        
        // Wait for libraries to load
        composeTestRule.waitForIdle()
        
        // Select Movies library
        composeTestRule.onNodeWithText("Movies").assertExists()
        composeTestRule.onNodeWithText("Movies").performClick()
        
        // Wait for media items to load
        composeTestRule.waitForIdle()
        
        // Select a movie
        composeTestRule.onNodeWithText("Test Movie")
            .assertExists()
            .performClick()
        
        // Verify navigation to room creation
        // Note: This part might need adjustment based on your actual navigation flow
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("jellyfin-Test Movie")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun loginFailure_showsError() {
        // Fill in login form with invalid credentials
        composeTestRule.onNodeWithText("Server URL")
            .performTextInput("http://test-jellyfin.local")
        composeTestRule.onNodeWithText("Username")
            .performTextInput("wronguser")
        composeTestRule.onNodeWithText("Password")
            .performTextInput("wrongpass")
        
        // Submit login
        composeTestRule.onNodeWithText("Connect").performClick()
        
        // Wait for error message
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Invalid credentials")
                .fetchSemanticsNodes().isNotEmpty()
        }
        
        // Verify we're still on login screen
        composeTestRule.onNodeWithText("Connect to Jellyfin").assertExists()
    }

    @Test
    fun librarySelection_showsMediaItems() {
        // First login
        performLogin()
        
        // Select a library
        composeTestRule.onNodeWithText("Movies").performClick()
        
        // Verify media items are displayed
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasAnyDescendant(hasText("Test Movie")))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun performLogin() {
        composeTestRule.onNodeWithText("Server URL")
            .performTextInput("http://test-jellyfin.local")
        composeTestRule.onNodeWithText("Username")
            .performTextInput("testuser")
        composeTestRule.onNodeWithText("Password")
            .performTextInput("password123")
        composeTestRule.onNodeWithText("Connect").performClick()
        
        // Wait for libraries to load
        composeTestRule.waitForIdle()
    }
}
