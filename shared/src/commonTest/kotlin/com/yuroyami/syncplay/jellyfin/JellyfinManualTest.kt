package com.yuroyami.syncplay.jellyfin

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.seconds

/**
 * Manual test utility for verifying Jellyfin integration with a real server.
 * 
 * Usage:
 * 1. Start a local Jellyfin server or have access to a test server
 * 2. Update the TEST_CONFIG values below
 * 3. Run the desired test methods
 */
@OptIn(DelicateCoroutinesApi::class)
object JellyfinManualTest {
    private object TEST_CONFIG {
        const val SERVER_URL = "http://localhost:8096"
        const val USERNAME = "test"
        const val PASSWORD = "test123"
    }

    private lateinit var repository: JellyfinRepository

    fun setup() {
        repository = JellyfinRepositoryImpl()
        JellyfinDebug.reset()

        // Subscribe to debug events
        GlobalScope.launch {
            JellyfinDebug.lastApiCall.collect { apiCall ->
                apiCall?.let { call ->
                    println("""
                        API Call:
                        Endpoint: ${call.endpoint}
                        Duration: ${call.duration}
                        Success: ${call.successful}
                        ${call.error?.let { "Error: $it" } ?: ""}
                        ---
                    """.trimIndent())
                }
            }
        }
    }

    fun testAuthentication() = runBlocking {
        println("Testing authentication...")
        val result = repository.authenticate(
            serverUrl = TEST_CONFIG.SERVER_URL,
            username = TEST_CONFIG.USERNAME,
            password = TEST_CONFIG.PASSWORD
        )

        if (result.isSuccess) {
            println("Authentication successful!")
            println("Config: ${result.getOrNull()}")
        } else {
            println("Authentication failed: ${result.exceptionOrNull()?.message}")
        }

        // Print performance metrics
        val metrics = JellyfinDebug.performanceMetrics.first()
        println("""
            Performance Metrics:
            Total Calls: ${metrics.totalCalls}
            Success Rate: ${metrics.successRate * 100}%
            Average Duration: ${metrics.averageCallDuration}
            Longest Call: ${metrics.longestCallDuration}
        """.trimIndent())
    }

    fun testLibraryAccess() = runBlocking {
        println("\nTesting library access...")
        // First authenticate
        repository.authenticate(
            serverUrl = TEST_CONFIG.SERVER_URL,
            username = TEST_CONFIG.USERNAME,
            password = TEST_CONFIG.PASSWORD
        ).getOrThrow()

        val result = repository.getLibraries()
        if (result.isSuccess) {
            println("Libraries loaded:")
            result.getOrNull()?.forEach { library ->
                println("- ${library.name} (${library.type})")
            }
        } else {
            println("Failed to load libraries: ${result.exceptionOrNull()?.message}")
        }
    }

    fun testMediaBrowsing() = runBlocking {
        println("\nTesting media browsing...")
        // First authenticate
        repository.authenticate(
            serverUrl = TEST_CONFIG.SERVER_URL,
            username = TEST_CONFIG.USERNAME,
            password = TEST_CONFIG.PASSWORD
        ).getOrThrow()

        // Get libraries
        val libraries = repository.getLibraries().getOrThrow()
        
        // Try to browse each library
        libraries.forEach { library ->
            println("\nBrowsing ${library.name}...")
            val items = repository.getMediaItems(library.id).getOrThrow()
            println("Found ${items.size} items:")
            items.take(5).forEach { item -> // Show first 5 items
                println("""
                    - ${item.name}
                      Type: ${item.type}
                      Has thumbnail: ${item.imageUrl != null}
                      ${item.seriesName?.let { "Series: $it" } ?: ""}
                """.trimIndent())
            }
            if (items.size > 5) println("... and ${items.size - 5} more items")
        }
    }

    fun testStreamUrlGeneration() = runBlocking {
        println("\nTesting stream URL generation...")
        // First authenticate
        repository.authenticate(
            serverUrl = TEST_CONFIG.SERVER_URL,
            username = TEST_CONFIG.USERNAME,
            password = TEST_CONFIG.PASSWORD
        ).getOrThrow()

        // Get first movie from library
        val libraries = repository.getLibraries().getOrThrow()
        val movieLibrary = libraries.find { it.type.contains("movies", ignoreCase = true) }
        requireNotNull(movieLibrary) { "No movie library found" }

        val movies = repository.getMediaItems(movieLibrary.id).getOrThrow()
        require(movies.isNotEmpty()) { "No movies found in library" }

        val movie = movies.first()
        println("Testing stream URL for: ${movie.name}")

        val streamUrl = repository.getStreamUrl(movie.id).getOrThrow()
        println("Stream URL generated: $streamUrl")
    }

    fun runAllTests() {
        setup()
        testAuthentication()
        testLibraryAccess()
        testMediaBrowsing()
        testStreamUrlGeneration()

        // Print final performance summary
        runBlocking {
            val metrics = JellyfinDebug.performanceMetrics.first()
            println("""
                
                Final Performance Summary:
                ==========================
                Total API Calls: ${metrics.totalCalls}
                Successful Calls: ${metrics.successfulCalls}
                Failed Calls: ${metrics.failedCalls}
                Success Rate: ${metrics.successRate * 100}%
                Average Duration: ${metrics.averageCallDuration}
                Longest Call: ${metrics.longestCallDuration}
                Total Duration: ${metrics.totalDuration}
            """.trimIndent())
        }
    }
}
