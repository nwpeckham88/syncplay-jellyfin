package com.yuroyami.syncplay.jellyfin

import com.yuroyami.syncplay.models.JoinInfo
import com.yuroyami.syncplay.models.MediaFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class JellyfinViewModelTest {
    private lateinit var viewModel: JellyfinViewModel
    private lateinit var testRepository: TestJellyfinRepository
    private lateinit var testDispatcher: TestDispatcher
    private var capturedJoinInfo: JoinInfo? = null

    @BeforeTest
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        
        testRepository = TestJellyfinRepository()
        viewModel = JellyfinViewModel(
            repository = testRepository,
            onJoinRoom = { joinInfo -> capturedJoinInfo = joinInfo }
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is login`() {
        assertEquals(JellyfinUiState.Login, viewModel.uiState)
        assertEquals(LoginState.Initial, viewModel.loginState)
        assertTrue(viewModel.libraries.isEmpty())
        assertNull(viewModel.selectedLibrary)
        assertTrue(viewModel.mediaItems.isEmpty())
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun `successful login updates state and loads libraries`() = runTest {
        // Given
        val serverUrl = "http://jellyfin.local"
        val username = "test"
        val password = "password"
        val config = JellyfinConfig("testServer", "testApiKey", "testUserId")
        testRepository.nextAuthResult = Result.success(config)
        testRepository.nextLibrariesResult = Result.success(listOf(
            JellyfinMediaItem("1", "Movies", "library", "")
        ))

        // When
        viewModel.login(serverUrl, username, password)
        advanceUntilIdle()

        // Then
        assertEquals(JellyfinUiState.Browser, viewModel.uiState)
        assertEquals(LoginState.Success, viewModel.loginState)
        assertEquals(1, viewModel.libraries.size)
        assertEquals("Movies", viewModel.libraries[0].name)
    }

    @Test
    fun `failed login shows error state`() = runTest {
        // Given
        testRepository.nextAuthResult = Result.failure(Exception("Invalid credentials"))

        // When
        viewModel.login("server", "user", "pass")
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.loginState is LoginState.Error)
        assertEquals(JellyfinUiState.Login, viewModel.uiState)
    }

    @Test
    fun `selecting library loads media items`() = runTest {
        // Given
        val library = JellyfinMediaItem("1", "Movies", "library", "")
        val mediaItems = listOf(
            JellyfinMediaItem("m1", "Movie 1", "movie", "overview")
        )
        testRepository.nextMediaItemsResult = Result.success(mediaItems)

        // When
        viewModel.selectLibrary(library)
        advanceUntilIdle()

        // Then
        assertEquals(library, viewModel.selectedLibrary)
        assertEquals(1, viewModel.mediaItems.size)
        assertEquals("Movie 1", viewModel.mediaItems[0].name)
    }

    @Test
    fun `selecting media creates room with stream URL`() = runTest {
        // Given
        val mediaItem = JellyfinMediaItem("m1", "Movie 1", "movie", "overview")
        val streamUrl = "http://jellyfin.local/videos/m1/stream"
        testRepository.nextStreamUrlResult = Result.success(streamUrl)

        // When
        viewModel.selectMedia(mediaItem)
        advanceUntilIdle()

        // Then
        assertNotNull(capturedJoinInfo)
        assertTrue(capturedJoinInfo?.roomname?.startsWith("jellyfin-") ?: false)
    }
}

private class TestJellyfinRepository : JellyfinRepository {
    var nextAuthResult: Result<JellyfinConfig>? = null
    var nextLibrariesResult: Result<List<JellyfinMediaItem>>? = null
    var nextMediaItemsResult: Result<List<JellyfinMediaItem>>? = null
    var nextStreamUrlResult: Result<String>? = null

    override suspend fun authenticate(
        serverUrl: String,
        username: String,
        password: String
    ): Result<JellyfinConfig> = 
        nextAuthResult ?: Result.failure(Exception("No test result configured"))

    override suspend fun getLibraries(): Result<List<JellyfinMediaItem>> =
        nextLibrariesResult ?: Result.failure(Exception("No test result configured"))

    override suspend fun getMediaItems(parentId: String): Result<List<JellyfinMediaItem>> =
        nextMediaItemsResult ?: Result.failure(Exception("No test result configured"))

    override suspend fun getStreamUrl(itemId: String): Result<String> =
        nextStreamUrlResult ?: Result.failure(Exception("No test result configured"))
}
