package com.yuroyami.syncplay.jellyfin

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.yuroyami.syncplay.models.JoinInfo
import org.junit.Rule
import org.junit.Test

class JellyfinScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_showsAllFields() {
        // Given
        val testViewModel = createTestViewModel()

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
        val repository = object : JellyfinRepository {
            override suspend fun authenticate(serverUrl: String, username: String, password: String): Result<JellyfinConfig> {
                kotlinx.coroutines.awaitCancellation()
            }

            override suspend fun getLibraries() = Result.success(emptyList<JellyfinMediaItem>())
            override suspend fun getMediaItems(parentId: String) = Result.success(emptyList<JellyfinMediaItem>())
            override suspend fun getStreamUrl(itemId: String) = Result.success("")
        }
        val testViewModel = createTestViewModel(repository)

        // When
        composeTestRule.setContent {
            JellyfinLoginScreen(viewModel = testViewModel)
        }
        composeTestRule.onNodeWithText("Server URL").performTextInput("http://test")
        composeTestRule.onNodeWithText("Username").performTextInput("test")
        composeTestRule.onNodeWithText("Password").performTextInput("password")
        composeTestRule.onNodeWithText("Connect").performClick()

        // Then
        composeTestRule.onNodeWithText("Connect").assertDoesNotExist()
    }

    @Test
    fun loginScreen_showsErrorMessage() {
        // Given
        val repository = object : JellyfinRepository {
            override suspend fun authenticate(serverUrl: String, username: String, password: String): Result<JellyfinConfig> {
                return Result.failure(Exception("Invalid credentials"))
            }

            override suspend fun getLibraries() = Result.success(emptyList<JellyfinMediaItem>())
            override suspend fun getMediaItems(parentId: String) = Result.success(emptyList<JellyfinMediaItem>())
            override suspend fun getStreamUrl(itemId: String) = Result.success("")
        }
        val testViewModel = createTestViewModel(repository)

        // When
        composeTestRule.setContent {
            JellyfinLoginScreen(viewModel = testViewModel)
        }
        composeTestRule.onNodeWithText("Server URL").performTextInput("http://test")
        composeTestRule.onNodeWithText("Username").performTextInput("test")
        composeTestRule.onNodeWithText("Password").performTextInput("password")
        composeTestRule.onNodeWithText("Connect").performClick()

        // Then
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Invalid credentials").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun browserScreen_showsLibraries() {
        // Given
        val repository = object : JellyfinRepository {
            override suspend fun authenticate(serverUrl: String, username: String, password: String): Result<JellyfinConfig> {
                return Result.success(JellyfinConfig("http://test", "token", "user"))
            }

            override suspend fun getLibraries(): Result<List<JellyfinMediaItem>> {
                return Result.success(
                    listOf(
                        JellyfinMediaItem("1", "Movies", "library", ""),
                        JellyfinMediaItem("2", "TV Shows", "library", "")
                    )
                )
            }

            override suspend fun getMediaItems(parentId: String) = Result.success(emptyList<JellyfinMediaItem>())
            override suspend fun getStreamUrl(itemId: String) = Result.success("")
        }
        val testViewModel = createTestViewModel(repository)
        testViewModel.login("http://test", "test", "password")

        // When
        composeTestRule.setContent {
            JellyfinBrowserScreen(viewModel = testViewModel)
        }
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            testViewModel.libraries.size == 2
        }

        // Then
        composeTestRule.onNodeWithText("Movies").assertExists()
        composeTestRule.onNodeWithText("TV Shows").assertExists()
    }

    @Test
    fun browserScreen_selectingLibraryLoadsItems() {
        // Given
        val repository = object : JellyfinRepository {
            override suspend fun authenticate(serverUrl: String, username: String, password: String): Result<JellyfinConfig> {
                return Result.success(JellyfinConfig("http://test", "token", "user"))
            }

            override suspend fun getLibraries(): Result<List<JellyfinMediaItem>> {
                return Result.success(listOf(JellyfinMediaItem("1", "Movies", "library", "")))
            }

            override suspend fun getMediaItems(parentId: String): Result<List<JellyfinMediaItem>> {
                return Result.success(listOf(JellyfinMediaItem("m1", "Test Movie", "movie", "A test movie")))
            }

            override suspend fun getStreamUrl(itemId: String) = Result.success("")
        }
        val testViewModel = createTestViewModel(repository)
        testViewModel.login("http://test", "test", "password")

        // When
        composeTestRule.setContent {
            JellyfinBrowserScreen(viewModel = testViewModel)
        }
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            testViewModel.libraries.isNotEmpty()
        }
        composeTestRule.onNodeWithText("Movies").performClick()

        // Then
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Test Movie").fetchSemanticsNodes().isNotEmpty()
        }
    }
}

private fun createTestViewModel(
    repository: JellyfinRepository = object : JellyfinRepository {
        override suspend fun authenticate(serverUrl: String, username: String, password: String) =
            Result.success(JellyfinConfig("", "", ""))

        override suspend fun getLibraries() = Result.success(emptyList<JellyfinMediaItem>())
        override suspend fun getMediaItems(parentId: String) = Result.success(emptyList<JellyfinMediaItem>())
        override suspend fun getStreamUrl(itemId: String) = Result.success("")
    }
) = JellyfinViewModel(
    repository = repository,
    onJoinRoom = {},
    restoreSavedConfig = false,
    restoreConfig = { null },
    persistConfig = {},
    loadJoinInfo = { JoinInfo() }
)
