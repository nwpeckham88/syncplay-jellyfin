package com.yuroyami.syncplay.jellyfin

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class JellyfinRepositoryImplTest {
    private lateinit var mockEngine: MockEngine
    private lateinit var repository: JellyfinRepositoryImpl

    @BeforeTest
    fun setup() {
        mockEngine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/Users/AuthenticateByName" -> {
                    respond(
                        content = """
                        {
                            "AccessToken": "test-token",
                            "ServerId": "test-server",
                            "UserId": "test-user"
                        }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/Users/test-user/Views" -> {
                    respond(
                        content = """
                        [
                            {
                                "Id": "1",
                                "Name": "Movies",
                                "Type": "CollectionFolder",
                                "ImageTags": {"Primary": "tag1"}
                            }
                        ]
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/Users/test-user/Items" -> {
                    respond(
                        content = """
                        [
                            {
                                "Id": "movie1",
                                "Name": "Test Movie",
                                "Type": "Movie",
                                "Overview": "A test movie",
                                "ImageTags": {"Primary": "tag1"}
                            }
                        ]
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                else -> {
                    error("Unhandled ${request.url.encodedPath}")
                }
            }
        }

        repository = JellyfinRepositoryImpl(mockEngine)
    }

    @Test
    fun `authenticate returns success with valid credentials`() = runTest {
        // When
        val result = repository.authenticate(
            serverUrl = "http://test.com",
            username = "test",
            password = "password"
        )

        // Then
        assertTrue(result.isSuccess)
        val config = result.getOrNull()!!
        assertEquals("test-token", config.apiKey)
        assertEquals("test-user", config.userId)
    }

    @Test
    fun `getLibraries returns media items`() = runTest {
        // Given
        repository.authenticate("http://test.com", "test", "password").getOrThrow()

        // When
        val result = repository.getLibraries()

        // Then
        assertTrue(result.isSuccess)
        val libraries = result.getOrNull()!!
        assertEquals(1, libraries.size)
        assertEquals("Movies", libraries[0].name)
    }

    @Test
    fun `getMediaItems returns items for library`() = runTest {
        // Given
        repository.authenticate("http://test.com", "test", "password").getOrThrow()

        // When
        val result = repository.getMediaItems("1")

        // Then
        assertTrue(result.isSuccess)
        val items = result.getOrNull()!!
        assertEquals(1, items.size)
        assertEquals("Test Movie", items[0].name)
    }

    @Test
    fun `getStreamUrl returns valid URL`() = runTest {
        // Given
        repository.authenticate("http://test.com", "test", "password").getOrThrow()

        // When
        val result = repository.getStreamUrl("movie1")

        // Then
        assertTrue(result.isSuccess)
        val url = result.getOrNull()!!
        assertTrue(url.contains("/Videos/movie1/stream"))
        assertTrue(url.contains("api_key=test-token"))
    }

    @Test
    fun `failed authentication returns error result`() = runTest {
        // Given
        mockEngine = MockEngine {
            respond(
                content = "Invalid credentials",
                status = HttpStatusCode.Unauthorized
            )
        }
        repository = JellyfinRepositoryImpl(mockEngine)

        // When
        val result = repository.authenticate(
            serverUrl = "http://test.com",
            username = "test",
            password = "wrong"
        )

        // Then
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }
}
