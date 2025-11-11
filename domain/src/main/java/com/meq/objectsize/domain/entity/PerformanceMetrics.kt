package com.meq.objectsize.domain.entity

data class PerformanceMetrics(
    val inferenceTimeMs: Long,
    val preprocessTimeMs: Long,
    val postprocessTimeMs: Long,
    val totalTimeMs: Long,
    val fps: Float,
    val memoryUsedMb: Float,
    val cpuUsagePercent: Float? = null
)
