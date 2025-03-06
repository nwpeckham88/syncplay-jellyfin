package com.yuroyami.syncplay.jellyfin

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

class JellyfinScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_showsAllFields() {
        // Given
        val testViewModel = TestJellyfinViewModel()

        // When
        composeTestRule.setContent {
            JellyfinLoginScreen(viewModel = testViewModel)
        }

        // Then
        composeTestRule.onNodeWithText("Connect to Jellyfin").assertExists()
        composeTestRule.onNodeWithText("Server URL").assertExists()
        composeTestRule.onNodeWithText("Username").assertExists()
        composeTestRule.onNodeWithText("Password").assertExists()
        composeTestRule.onNodeWithText("Connect").assertExists().assertIsEnabled()
    }

    @Test
    fun loginScreen_disablesButtonDuringLoading() {
        // Given
        val testViewModel = TestJellyfinViewModel()
        testViewModel.setLoginState(LoginState.Loading)

        // When
        composeTestRule.setContent {
            JellyfinLoginScreen(viewModel = testViewModel)
        }

        // Then
        composeTestRule.onNodeWithText("Connect").assertIsNotEnabled()
    }

    @Test
    fun loginScreen_showsErrorMessage() {
        // Given
        val testViewModel = TestJellyfinViewModel()
        testViewModel.setLoginState(LoginState.Error("Invalid credentials"))

        // When
        composeTestRule.setContent {
            JellyfinLoginScreen(viewModel = testViewModel)
        }

        // Then
        composeTestRule.onNodeWithText("Invalid credentials").assertExists()
    }

    @Test
    fun browserScreen_showsLibraries() {
        // Given
        val testViewModel = TestJellyfinViewModel()
        testViewModel.setLibraries(listOf(
            JellyfinMediaItem("1", "Movies", "library", ""),
            JellyfinMediaItem("2", "TV Shows", "library", "")
        ))

        // When
        composeTestRule.setContent {
            JellyfinBrowserScreen(viewModel = testViewModel)
        }

        // Then
        composeTestRule.onNodeWithText("Movies").assertExists()
        composeTestRule.onNodeWithText("TV Shows").assertExists()
    }

    @Test
    fun browserScreen_selectingLibraryLoadsItems() {
        // Given
        val testViewModel = TestJellyfinViewModel()
        testViewModel.setLibraries(listOf(
            JellyfinMediaItem("1", "Movies", "library", "")
        ))
        testViewModel.setMediaItems(listOf(
            JellyfinMediaItem("m1", "Test Movie", "movie", "A test movie")
        ))

        // When
        composeTestRule.setContent {
            JellyfinBrowserScreen(viewModel = testViewModel)
        }
        composeTestRule.onNodeWithText("Movies").performClick()

        // Then
        composeTestRule.onNodeWithText("Test Movie").assertExists()
    }
}

private class TestJellyfinViewModel : JellyfinViewModel(
    repository = object : JellyfinRepository {
        override suspend fun authenticate(serverUrl: String, username: String, password: String) = 
            Result.success(JellyfinConfig("", "", ""))
        override suspend fun getLibraries() = Result.success(emptyList<JellyfinMediaItem>())
        override suspend fun getMediaItems(parentId: String) = Result.success(emptyList<JellyfinMediaItem>())
        override suspend fun getStreamUrl(itemId: String) = Result.success("")
    },
    onJoinRoom = {}
) {
    fun setLoginState(state: LoginState) {
        loginState = state
    }

    fun setLibraries(items: List<JellyfinMediaItem>) {
        libraries = items
    }

    fun setMediaItems(items: List<JellyfinMediaItem>) {
        mediaItems = items
    }
}
