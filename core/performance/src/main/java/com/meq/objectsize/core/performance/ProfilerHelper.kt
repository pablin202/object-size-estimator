package com.meq.objectsize.core.performance

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import com.meq.objectsize.domain.entity.PerformanceMetrics
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Professional profiler helper for capturing performance and memory snapshots
 *
 * Features:
 * - Memory snapshots with system metrics
 * - Performance metrics integration
 * - Async file operations (non-blocking)
 * - Auto-cleanup of old reports
 * - JSON export for programmatic analysis
 * - Share functionality
 *
 * Only active in DEBUG builds to avoid overhead in production
 */
@Singleton
class ProfilerHelper @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val performanceMonitor: PerformanceMonitor
) {
    private val runtime = Runtime.getRuntime()
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    // Configuration
    private val maxReports = 10  // Keep only last 10 reports
    private val reportsDir = File(context.getExternalFilesDir(null), "profiler_reports")

    companion object {
        private const val TAG = "ProfilerHelper"
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
    }

    init {
        // Create reports directory if needed
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }
    }

    /**
     * Capture comprehensive snapshot including memory and performance
     * Non-blocking - runs on IO dispatcher
     */
    suspend fun captureSnapshot(detectionCount: Int = 0, objectsDetected: Int = 0) {
        // Only capture in debug builds to avoid overhead
        if (!isDebugBuild()) return

        withContext(Dispatchers.IO) {
            try {
                val snapshot = buildSnapshot(detectionCount, objectsDetected)
                val timestamp = dateFormat.format(Date())

                // Save as JSON
                val jsonFile = File(reportsDir, "snapshot_$timestamp.json")
                jsonFile.writeText(snapshot.toJson())

                // Save as human-readable text
                val txtFile = File(reportsDir, "snapshot_$timestamp.txt")
                txtFile.writeText(snapshot.toReadableString())

                Log.i(TAG, "Snapshot saved: ${jsonFile.absolutePath}")

                // Cleanup old reports
                cleanupOldReports()

            } catch (e: Exception) {
                Log.e(TAG, "Error capturing snapshot", e)
            }
        }
    }

    /**
     * Build snapshot data structure
     */
    private fun buildSnapshot(detectionCount: Int, objectsDetected: Int): SnapshotData {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        return SnapshotData(
            timestamp = System.currentTimeMillis(),
            // App Memory
            usedMemoryMb = (runtime.totalMemory() - runtime.freeMemory()) / 1024f / 1024f,
            freeMemoryMb = runtime.freeMemory() / 1024f / 1024f,
            totalMemoryMb = runtime.totalMemory() / 1024f / 1024f,
            maxMemoryMb = runtime.maxMemory() / 1024f / 1024f,
            // System Memory
            systemAvailableMb = memoryInfo.availMem / 1024f / 1024f,
            systemTotalMb = memoryInfo.totalMem / 1024f / 1024f,
            lowMemory = memoryInfo.lowMemory,
            // Performance Metrics
            avgInferenceMs = performanceMonitor.getAverageInferenceTime(),
            avgFps = performanceMonitor.getAverageFps(),
            currentMemoryMb = performanceMonitor.getCurrentMemoryMb(),
            // Detection Stats
            detectionCount = detectionCount,
            objectsDetected = objectsDetected,
            // Device Info
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
            androidVersion = Build.VERSION.SDK_INT
        )
    }

    /**
     * Capture snapshot triggered by performance metrics
     */
    suspend fun capturePerformanceSnapshot(
        metrics: PerformanceMetrics,
        detectionCount: Int,
        objectsDetected: Int
    ) {
        if (!isDebugBuild()) return

        withContext(Dispatchers.IO) {
            try {
                val snapshot = buildSnapshot(detectionCount, objectsDetected)
                val timestamp = dateFormat.format(Date())

                // Enhanced snapshot with current frame metrics
                val enhancedData = JSONObject().apply {
                    put("snapshot", JSONObject(snapshot.toJson()))
                    put("current_frame", JSONObject().apply {
                        put("inference_ms", metrics.inferenceTimeMs)
                        put("preprocess_ms", metrics.preprocessTimeMs)
                        put("postprocess_ms", metrics.postprocessTimeMs)
                        put("total_ms", metrics.totalTimeMs)
                        put("fps", metrics.fps)
                        put("memory_mb", metrics.memoryUsedMb)
                    })
                }

                val file = File(reportsDir, "perf_snapshot_$timestamp.json")
                file.writeText(enhancedData.toString(2))

                Log.i(TAG, "Performance snapshot saved: ${file.absolutePath}")

                cleanupOldReports()

            } catch (e: Exception) {
                Log.e(TAG, "Error capturing performance snapshot", e)
            }
        }
    }

    /**
     * Generate full report with all available metrics
     */
    suspend fun generateFullReport(): File? {
        if (!isDebugBuild()) return null

        return withContext(Dispatchers.IO) {
            try {
                val snapshot = buildSnapshot(0, 0)
                val timestamp = dateFormat.format(Date())

                val report = buildString {
                    appendLine("═══════════════════════════════════════════════════════")
                    appendLine("           OBJECT SIZE ESTIMATOR - FULL REPORT           ")
                    appendLine("═══════════════════════════════════════════════════════")
                    appendLine()
                    appendLine("Generated: ${Date(snapshot.timestamp)}")
                    appendLine("Device: ${snapshot.deviceModel}")
                    appendLine("Android API: ${snapshot.androidVersion}")
                    appendLine()
                    appendLine("───────────────────────────────────────────────────────")
                    appendLine("                    MEMORY METRICS                      ")
                    appendLine("───────────────────────────────────────────────────────")
                    appendLine("App Memory:")
                    appendLine("  Used:  ${String.format("%.1f", snapshot.usedMemoryMb)} MB")
                    appendLine("  Free:  ${String.format("%.1f", snapshot.freeMemoryMb)} MB")
                    appendLine("  Total: ${String.format("%.1f", snapshot.totalMemoryMb)} MB")
                    appendLine("  Max:   ${String.format("%.1f", snapshot.maxMemoryMb)} MB")
                    appendLine()
                    appendLine("System Memory:")
                    appendLine("  Available: ${String.format("%.1f", snapshot.systemAvailableMb)} MB")
                    appendLine("  Total:     ${String.format("%.1f", snapshot.systemTotalMb)} MB")
                    appendLine("  Low Memory: ${if (snapshot.lowMemory) "YES ⚠️" else "NO ✓"}")
                    appendLine()
                    appendLine("───────────────────────────────────────────────────────")
                    appendLine("                 PERFORMANCE METRICS                    ")
                    appendLine("───────────────────────────────────────────────────────")
                    appendLine("Average Inference: ${String.format("%.1f", snapshot.avgInferenceMs)} ms")
                    appendLine("Average FPS:       ${String.format("%.1f", snapshot.avgFps)}")
                    appendLine("Current Memory:    ${String.format("%.1f", snapshot.currentMemoryMb)} MB")
                    appendLine()
                    appendLine("───────────────────────────────────────────────────────")
                    appendLine("                  DETECTION STATS                       ")
                    appendLine("───────────────────────────────────────────────────────")
                    appendLine("Total Detections: ${snapshot.detectionCount}")
                    appendLine("Objects Detected: ${snapshot.objectsDetected}")
                    appendLine()
                    appendLine("═══════════════════════════════════════════════════════")
                }

                val file = File(reportsDir, "full_report_$timestamp.txt")
                file.writeText(report)

                Log.i(TAG, "Full report generated: ${file.absolutePath}")

                file
            } catch (e: Exception) {
                Log.e(TAG, "Error generating full report", e)
                null
            }
        }
    }

    /**
     * Share report via Intent (email, drive, etc.)
     */
    suspend fun shareReport(context: Context): Boolean {
        val reportFile = generateFullReport() ?: return false

        return withContext(Dispatchers.Main) {
            try {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    reportFile
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Performance Report - Object Size Estimator")
                    putExtra(Intent.EXTRA_TEXT, "Performance profiling report attached.")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(Intent.createChooser(shareIntent, "Share Report"))
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error sharing report", e)
                false
            }
        }
    }

    /**
     * Clean up old reports to avoid filling storage
     */
    private fun cleanupOldReports() {
        try {
            val files = reportsDir.listFiles() ?: return

            // Sort by modification time (oldest first)
            val sortedFiles = files.sortedBy { it.lastModified() }

            // Delete oldest files if we exceed maxReports
            if (sortedFiles.size > maxReports) {
                val filesToDelete = sortedFiles.size - maxReports
                sortedFiles.take(filesToDelete).forEach { file ->
                    file.delete()
                    Log.d(TAG, "Deleted old report: ${file.name}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up reports", e)
        }
    }

    /**
     * Get all available reports
     */
    fun getReports(): List<File> {
        return reportsDir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    /**
     * Clear all reports
     */
    suspend fun clearAllReports() {
        withContext(Dispatchers.IO) {
            try {
                reportsDir.listFiles()?.forEach { it.delete() }
                Log.i(TAG, "All reports cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing reports", e)
            }
        }
    }

    /**
     * Check if running in debug mode
     * Uses context instead of BuildConfig to avoid package visibility issues
     */
    private fun isDebugBuild(): Boolean {
        return context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
    }
}

/**
 * Data class for snapshot information
 */
private data class SnapshotData(
    val timestamp: Long,
    // Memory
    val usedMemoryMb: Float,
    val freeMemoryMb: Float,
    val totalMemoryMb: Float,
    val maxMemoryMb: Float,
    val systemAvailableMb: Float,
    val systemTotalMb: Float,
    val lowMemory: Boolean,
    // Performance
    val avgInferenceMs: Float,
    val avgFps: Float,
    val currentMemoryMb: Float,
    // Stats
    val detectionCount: Int,
    val objectsDetected: Int,
    // Device
    val deviceModel: String,
    val androidVersion: Int
) {
    fun toJson(): String {
        return JSONObject().apply {
            put("timestamp", timestamp)
            put("datetime", Date(timestamp).toString())
            put("memory", JSONObject().apply {
                put("app_used_mb", usedMemoryMb)
                put("app_free_mb", freeMemoryMb)
                put("app_total_mb", totalMemoryMb)
                put("app_max_mb", maxMemoryMb)
                put("system_available_mb", systemAvailableMb)
                put("system_total_mb", systemTotalMb)
                put("low_memory", lowMemory)
            })
            put("performance", JSONObject().apply {
                put("avg_inference_ms", avgInferenceMs)
                put("avg_fps", avgFps)
                put("current_memory_mb", currentMemoryMb)
            })
            put("stats", JSONObject().apply {
                put("detection_count", detectionCount)
                put("objects_detected", objectsDetected)
            })
            put("device", JSONObject().apply {
                put("model", deviceModel)
                put("android_api", androidVersion)
            })
        }.toString(2)
    }

    fun toReadableString(): String {
        return buildString {
            appendLine("Snapshot - ${Date(timestamp)}")
            appendLine()
            appendLine("Memory:")
            appendLine("  Used: ${String.format("%.1f", usedMemoryMb)} MB")
            appendLine("  Total: ${String.format("%.1f", totalMemoryMb)} MB")
            appendLine("  Max: ${String.format("%.1f", maxMemoryMb)} MB")
            appendLine("  System Available: ${String.format("%.1f", systemAvailableMb)} MB")
            appendLine("  Low Memory: $lowMemory")
            appendLine()
            appendLine("Performance:")
            appendLine("  Avg Inference: ${String.format("%.1f", avgInferenceMs)} ms")
            appendLine("  Avg FPS: ${String.format("%.1f", avgFps)}")
            appendLine()
            appendLine("Stats:")
            appendLine("  Detections: $detectionCount")
            appendLine("  Objects: $objectsDetected")
            appendLine()
            appendLine("Device: $deviceModel (API $androidVersion)")
        }
    }
}
