package com.yuroyami.syncplay.jellyfin

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Debug utility for Jellyfin integration testing
 */
object JellyfinDebug {
    private val _lastApiCall = MutableStateFlow<ApiCall?>(null)
    val lastApiCall = _lastApiCall.asStateFlow()

    private val _performanceMetrics = MutableStateFlow(PerformanceMetrics())
    val performanceMetrics = _performanceMetrics.asStateFlow()

    fun logApiCall(call: ApiCall) {
        _lastApiCall.value = call
    }

    fun updateMetrics(update: PerformanceMetrics.() -> Unit) {
        _performanceMetrics.value = _performanceMetrics.value.copy().apply(update)
    }

    fun reset() {
        _lastApiCall.value = null
        _performanceMetrics.value = PerformanceMetrics()
    }

    fun measureCall(endpoint: String, block: suspend () -> Result<*>): Result<*> {
        val startTime = System.nanoTime()
        return try {
            block().also { result ->
                val duration = (System.nanoTime() - startTime).toDuration(DurationUnit.NANOSECONDS)
                logApiCall(ApiCall(
                    endpoint = endpoint,
                    method = "GET", // Simplified for now
                    duration = duration,
                    successful = result.isSuccess,
                    error = result.exceptionOrNull()?.message
                ))
                
                updateMetrics {
                    totalCalls++
                    totalDuration += duration
                    if (result.isSuccess) successfulCalls++ else failedCalls++
                    if (duration > longestCallDuration) longestCallDuration = duration
                }
            }
        } catch (e: Exception) {
            val duration = (System.nanoTime() - startTime).toDuration(DurationUnit.NANOSECONDS)
            logApiCall(ApiCall(
                endpoint = endpoint,
                method = "GET",
                duration = duration,
                successful = false,
                error = e.message
            ))
            Result.failure(e)
        }
    }
}

data class ApiCall(
    val endpoint: String,
    val method: String,
    val duration: Duration,
    val successful: Boolean,
    val error: String? = null
)

data class PerformanceMetrics(
    var totalCalls: Int = 0,
    var successfulCalls: Int = 0,
    var failedCalls: Int = 0,
    var totalDuration: Duration = Duration.ZERO,
    var longestCallDuration: Duration = Duration.ZERO
) {
    val averageCallDuration: Duration
        get() = if (totalCalls > 0) {
            totalDuration / totalCalls
        } else Duration.ZERO

    val successRate: Double
        get() = if (totalCalls > 0) {
            successfulCalls.toDouble() / totalCalls
        } else 0.0
}
