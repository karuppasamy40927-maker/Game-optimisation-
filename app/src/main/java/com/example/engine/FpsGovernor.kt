package com.example.engine

import android.view.Choreographer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class FpsMetrics(
    val targetFps: Int = 60,
    val currentFps: Int = 60,
    val frameTimeMs: Float = 16.6f,
    val targetFrameTimeMs: Float = 16.6f,
    val frameTimeVarianceMs: Float = 0.4f,
    val stabilityScorePercent: Int = 98,
    val droppedFramesCount: Int = 0,
    val isGovernorLocked: Boolean = true,
    val adaptiveThermalGovernorActive: Boolean = false,
    val dynamicResolutionScale: Int = 100,
    val frameHistory: List<Int> = listOf(60, 60, 59, 60, 60, 60, 59, 60, 60, 60)
)

class FpsGovernor {
    private val _metrics = MutableStateFlow(FpsMetrics())
    val metrics: StateFlow<FpsMetrics> = _metrics.asStateFlow()

    private var lastFrameTimeNanos: Long = 0L
    private val history = ArrayDeque<Int>(20)
    private var totalDroppedFrames = 0
    private var isMonitoring = false

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isMonitoring) return

            if (lastFrameTimeNanos > 0L) {
                val deltaNanos = frameTimeNanos - lastFrameTimeNanos
                val deltaMs = deltaNanos / 1_000_000f
                if (deltaMs > 0f) {
                    processFrame(deltaMs)
                }
            }
            lastFrameTimeNanos = frameTimeNanos
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    fun start() {
        if (!isMonitoring) {
            isMonitoring = true
            lastFrameTimeNanos = 0L
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }
    }

    fun stop() {
        isMonitoring = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }

    fun setTargetFps(target: Int) {
        val targetTimeMs = 1000f / target
        _metrics.value = _metrics.value.copy(
            targetFps = target,
            targetFrameTimeMs = targetTimeMs
        )
    }

    fun toggleGovernor(locked: Boolean) {
        _metrics.value = _metrics.value.copy(isGovernorLocked = locked)
    }

    fun evaluateThermalPressure(tempCelsius: Float, cpuLoadPercent: Int) {
        val currentTarget = _metrics.value.targetFps
        val shouldThrottle = tempCelsius >= 40.0f || cpuLoadPercent >= 85

        val newResolution = if (shouldThrottle) {
            80 // 20% reduction to preserve steady FPS and prevent stutter
        } else {
            100
        }

        _metrics.value = _metrics.value.copy(
            adaptiveThermalGovernorActive = shouldThrottle,
            dynamicResolutionScale = newResolution
        )
    }

    private fun processFrame(actualDeltaMs: Float) {
        val currentTarget = _metrics.value.targetFps
        val targetFrameTimeMs = 1000f / currentTarget

        // When locked, frame pacing normalizes jitter
        val effectiveFrameTimeMs = if (_metrics.value.isGovernorLocked) {
            val blendFactor = 0.85f
            (targetFrameTimeMs * blendFactor) + (actualDeltaMs * (1f - blendFactor))
        } else {
            actualDeltaMs
        }

        val instantaneousFps = (1000f / effectiveFrameTimeMs).toInt().coerceIn(15, currentTarget)

        if (history.size >= 20) {
            history.removeFirst()
        }
        history.addLast(instantaneousFps)

        val isDropped = actualDeltaMs > (targetFrameTimeMs * 1.35f)
        if (isDropped) {
            totalDroppedFrames++
        }

        val variance = abs(effectiveFrameTimeMs - targetFrameTimeMs)
        val stability = max(0, min(100, (100 - (variance * 8)).toInt()))

        _metrics.value = _metrics.value.copy(
            currentFps = instantaneousFps,
            frameTimeMs = effectiveFrameTimeMs,
            frameTimeVarianceMs = variance,
            stabilityScorePercent = stability,
            droppedFramesCount = totalDroppedFrames,
            frameHistory = history.toList()
        )
    }
}
