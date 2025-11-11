package com.meq.objectsize.core.performance

import com.meq.objectsize.domain.entity.PerformanceMetrics

class PerformanceMonitor {
    private val inferenceTimes = mutableListOf<Long>()
    private val maxSamples = 30

    private var lastFrameTime = System.currentTimeMillis()
    private val runtime = Runtime.getRuntime()

    fun recordInference(metrics: PerformanceMetrics) {
        inferenceTimes.add(metrics.inferenceTimeMs)
        if (inferenceTimes.size > maxSamples) {
            inferenceTimes.removeAt(0)
        }
    }

    fun getAverageInferenceTime(): Float {
        return if (inferenceTimes.isEmpty()) 0f
        else inferenceTimes.average().toFloat()
    }

    fun getAverageFps(): Float {
        return if (inferenceTimes.isEmpty()) 0f
        else 1000f / getAverageInferenceTime()
    }

    fun getCurrentMemoryMb(): Float {
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        return usedMemory / (1024f * 1024f)
    }

    fun getMaxMemoryMb(): Float {
        return runtime.maxMemory() / (1024f * 1024f)
    }

    fun calculateFps(): Float {
        val currentTime = System.currentTimeMillis()
        val deltaTime = currentTime - lastFrameTime
        lastFrameTime = currentTime
        return if (deltaTime > 0) 1000f / deltaTime else 0f
    }

    fun reset() {
        inferenceTimes.clear()
        lastFrameTime = System.currentTimeMillis()
    }
}
