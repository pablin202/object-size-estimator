package com.meq.objectsize.ml

import android.content.Context
import android.graphics.Bitmap
import android.os.Trace
import android.util.Log
import com.meq.objectsize.domain.model.BoundingBox
import com.meq.objectsize.domain.model.DetectionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import androidx.core.graphics.scale
import com.meq.objectsize.domain.PerformanceMonitor
import com.meq.objectsize.domain.ProfilerHelper
import com.meq.objectsize.domain.model.PerformanceMetrics
import com.meq.objectsize.utils.LeakCanaryWatchers
import org.tensorflow.lite.DataType

/**
 * Helper function to safely wrap code with Trace sections
 * Ensures endSection is always called even if an exception occurs
 */
private inline fun <T> trace(sectionName: String, block: () -> T): T {
    Trace.beginSection(sectionName)
    return try {
        block()
    } finally {
        Trace.endSection()
    }
}

/**
 * TensorFlow Lite implementation of ObjectDetector using SSD MobileNet v1
 *
 * Model details:
 * - Input: 300x300 RGB image (normalized 0-255)
 * - Output: 4 arrays
 *   1. locations [1][10][4] - bounding boxes
 *   2. classes [1][10] - class indices
 *   3. scores [1][10] - confidence scores
 *   4. numDetections [1] - count of valid detections
 *
 * @param context Android context for accessing assets
 * @param useGpu Whether to use GPU delegate (faster but requires compatible device)
 * @param performanceMonitor Performance metrics monitor
 * @param profilerHelper Profiler helper for capturing performance snapshots
 * @param confidenceThreshold Minimum confidence score for a detection to be considered
 */
class TFLiteObjectDetector(
    private val context: Context,
    private val useGpu: Boolean = false,
    private val performanceMonitor: PerformanceMonitor,
    private val profilerHelper: ProfilerHelper? = null,
    private val confidenceThreshold: Float = ObjectDetector.DEFAULT_CONFIDENCE_THRESHOLD
) : ObjectDetector {

    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null
    private var labels: List<String> = emptyList()
    private var inputImageBuffer: ByteBuffer? = null

    // CoroutineScope for Flow emissions
    // SupervisorJob: failures in one coroutine don't affect others
    // Dispatchers.Default: CPU-intensive work
    private val detectorScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // SharedFlow for performance metrics
    // replay = 0: Don't cache old values (hot flow)
    // extraBufferCapacity = 64: Buffer for backpressure handling
    private val _metricsFlow = MutableSharedFlow<PerformanceMetrics>(
        replay = 0,
        extraBufferCapacity = 64
    )
    override val metricsFlow: SharedFlow<PerformanceMetrics> = _metricsFlow.asSharedFlow()

    // Detection tracking for profiler snapshots
    private var detectionCount = 0
    private val snapshotInterval = 50  // Capture snapshot every 50 detections

    companion object {
        private const val TAG = "TFLiteObjectDetector"
        private const val MODEL_PATH = "ssd_mobilenet_v1.tflite"
        private const val LABELS_PATH = "labelmap.txt"

        private const val INPUT_SIZE = 300
        private const val PIXEL_SIZE = 3  // RGB channels
        private const val NUM_BYTES_PER_CHANNEL = 1  // UInt8 = 1 byte (no Float32 = 4 bytes)

        // Índices de outputs del modelo
        private const val OUTPUT_LOCATIONS = 0
        private const val OUTPUT_CLASSES = 1
        private const val OUTPUT_SCORES = 2
        private const val OUTPUT_NUM_DETECTIONS = 3
    }

    init {
        initialize()
    }

    /**
     * Initialize TFLite interpreter and load labels
     */
    private fun initialize() {
        try {
            // Load model from assets
            val modelBuffer = loadModelFile()

            // Configure interpreter options
            val compatList = CompatibilityList()
            val options = Interpreter.Options().apply {
                // GPU delegate for faster inference (optional)
                if (useGpu && compatList.isDelegateSupportedOnThisDevice) {
                    try {
                        val delegateOptions = compatList.bestOptionsForThisDevice
                        gpuDelegate = GpuDelegate(delegateOptions)
                        addDelegate(gpuDelegate)
                        Log.d(TAG, "✓ GPU delegate enabled with optimized options")
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to initialize GPU delegate, falling back to CPU", e)
                        setNumThreads(4)
                    }
                } else {
                    // Run on CPU with 4 threads
                    setNumThreads(4)
                    if (useGpu) {
                        Log.d(TAG, "GPU requested but not supported on this device, using CPU")
                    }
                }
            }

            interpreter = Interpreter(modelBuffer, options)

            val inputTensor = interpreter?.getInputTensor(0)

            when (inputTensor?.dataType()) {
                DataType.UINT8 -> {
                    Log.d(TAG, "✓ Model uses UINT8 (1 byte per value)")
                    // NUM_BYTES_PER_CHANNEL = 1 ✓
                }
                DataType.FLOAT32 -> {
                    Log.e(TAG, "✗ Model uses FLOAT32 (4 bytes per value)")
                    Log.e(TAG, "Update NUM_BYTES_PER_CHANNEL to 4 and use putFloat() in preprocessing")
                }
                else -> {
                    Log.w(TAG, "Unexpected data type: ${inputTensor?.dataType()}")
                }
            }

            inputTensor?.let {
                val shape = it.shape()
                val dataType = it.dataType()
                Log.d(TAG, "Model input shape: ${shape.contentToString()}")
                Log.d(TAG, "Model input dataType: $dataType")

                require(shape[1] == INPUT_SIZE && shape[2] == INPUT_SIZE) {
                    "Model expects ${shape[1]}x${shape[2]}, but configured for ${INPUT_SIZE}x${INPUT_SIZE}"
                }
            }

            // Load labels
            labels = context.assets.open(LABELS_PATH).bufferedReader().use { reader ->
                reader.readLines()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            }

            // Pre-allocate input buffer (UInt8: 1 byte per value)
            val bufferSize = NUM_BYTES_PER_CHANNEL * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE
            inputImageBuffer = ByteBuffer.allocateDirect(bufferSize).apply {
                order(ByteOrder.nativeOrder())
            }

            Log.d(TAG, "TFLite detector initialized successfully")
            Log.d(TAG, "Buffer size: $bufferSize bytes (${INPUT_SIZE}x${INPUT_SIZE}x${PIXEL_SIZE})")
            Log.d(TAG, "Labels loaded: ${labels.size}")

        } catch (e: Exception) {
            throw IllegalStateException("Failed to initialize TFLite detector: ${e.message}", e)
        }
    }

    /**
     * Load TFLite model from assets
     */
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    override suspend fun detect(bitmap: Bitmap): List<DetectionResult> = trace("ML_Detection") {
        withContext(Dispatchers.Default) {
            val totalStartTime = System.nanoTime()

            val currentInterpreter = interpreter
                ?: throw IllegalStateException("Detector not initialized")

            val buffer = inputImageBuffer
                ?: throw IllegalStateException("Input buffer not initialized")

            // 1. Preprocessing
            val preprocessStart = System.nanoTime()
            val preprocessTime = trace("Preprocessing") {
                preprocessBitmap(bitmap, buffer)
                (System.nanoTime() - preprocessStart) / 1_000_000 // ms
            }

            // Prepare output arrays
            val outputLocations = Array(1) { Array(ObjectDetector.MAX_DETECTIONS) { FloatArray(4) } }
            val outputClasses = Array(1) { FloatArray(ObjectDetector.MAX_DETECTIONS) }
            val outputScores = Array(1) { FloatArray(ObjectDetector.MAX_DETECTIONS) }
            val numDetections = FloatArray(1)

            // 2. Inference
            val inferenceStart = System.nanoTime()
            val inferenceTime = trace("TFLite_Inference") {
                val outputs = mapOf(
                    OUTPUT_LOCATIONS to outputLocations,
                    OUTPUT_CLASSES to outputClasses,
                    OUTPUT_SCORES to outputScores,
                    OUTPUT_NUM_DETECTIONS to numDetections
                )
                currentInterpreter.runForMultipleInputsOutputs(arrayOf(buffer), outputs)
                (System.nanoTime() - inferenceStart) / 1_000_000 // ms
            }

            // 3. Postprocessing
            val postprocessStart = System.nanoTime()
            val (results, postprocessTime) = trace("Postprocessing") {
                val res = parseDetections(
                    locations = outputLocations[0],
                    classes = outputClasses[0],
                    scores = outputScores[0],
                    numDetections = numDetections[0].toInt()
                )
                val time = (System.nanoTime() - postprocessStart) / 1_000_000 // ms
                res to time
            }

            // Calculate metrics
            val totalTime = (System.nanoTime() - totalStartTime) / 1_000_000 // ms
            val fps = performanceMonitor.calculateFps()
            val memoryMb = performanceMonitor.getCurrentMemoryMb()

            val metrics = PerformanceMetrics(
                inferenceTimeMs = inferenceTime,
                preprocessTimeMs = preprocessTime,
                postprocessTimeMs = postprocessTime,
                totalTimeMs = totalTime,
                fps = fps,
                memoryUsedMb = memoryMb
            )

            performanceMonitor.recordInference(metrics)

            // Emit metrics via Flow (non-blocking)
            // tryEmit returns false if buffer is full (backpressure)
            val emitted = _metricsFlow.tryEmit(metrics)
            if (!emitted) {
                Log.w(TAG, "Metrics buffer full, dropping sample (backpressure)")
            }

            // Log cada 30 frames
            if (performanceMonitor.getAverageInferenceTime() > 0 &&
                System.currentTimeMillis() % 1000 < 100) {
                logPerformanceMetrics(metrics)
            }

            // Capture profiler snapshot periodically
            detectionCount++
            if (profilerHelper != null && detectionCount % snapshotInterval == 0) {
                // Launch in detectorScope (will be cancelled on close())
                detectorScope.launch(Dispatchers.IO) {
                    profilerHelper.capturePerformanceSnapshot(
                        metrics = metrics,
                        detectionCount = detectionCount,
                        objectsDetected = results.size
                    )
                }
            }

            results
        }
    }

    private fun logPerformanceMetrics(metrics: PerformanceMetrics) {
        val avgInference = performanceMonitor.getAverageInferenceTime()
        val avgFps = performanceMonitor.getAverageFps()
        val maxMemory = performanceMonitor.getMaxMemoryMb()

        Log.d(TAG, """
            ═══════════════════════════════════════
            Performance Metrics:
            ───────────────────────────────────────
            Preprocess:  ${metrics.preprocessTimeMs}ms
            Inference:   ${metrics.inferenceTimeMs}ms
            Postprocess: ${metrics.postprocessTimeMs}ms
            Total:       ${metrics.totalTimeMs}ms
            ───────────────────────────────────────
            Current FPS: ${String.format("%.1f", metrics.fps)}
            Average FPS: ${String.format("%.1f", avgFps)}
            Avg Inference: ${String.format("%.1f", avgInference)}ms
            ───────────────────────────────────────
            Memory: ${String.format("%.1f", metrics.memoryUsedMb)}MB / ${String.format("%.1f", maxMemory)}MB
            ═══════════════════════════════════════
        """.trimIndent())
    }

    /**
     * Preprocess bitmap to model input format
     * - Resize to 300x300
     * - Convert to UInt8
     * - Keep in 0-255 range (no normalization needed)
     */
    private fun preprocessBitmap(bitmap: Bitmap, buffer: ByteBuffer) {
        buffer.rewind()

        // Resize bitmap to model input size
        val resizedBitmap = bitmap.scale(INPUT_SIZE, INPUT_SIZE)

        // Extract pixels
        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)
        resizedBitmap.getPixels(intValues, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        // Convert to bytes (UInt8 format: 0-255)
        var pixel = 0
        repeat(INPUT_SIZE) {
            repeat(INPUT_SIZE) {
                val value = intValues[pixel++]

                // Extract RGB channels as bytes (0-255)
                buffer.put(((value shr 16) and 0xFF).toByte())  // R
                buffer.put(((value shr 8) and 0xFF).toByte())   // G
                buffer.put((value and 0xFF).toByte())            // B
            }
        }

        // Clean up resized bitmap if it's different from original
        if (resizedBitmap != bitmap) {
            resizedBitmap.recycle()
        }
    }

    /**
     * Parse raw model outputs into DetectionResult objects
     */
    private fun parseDetections(
        locations: Array<FloatArray>,
        classes: FloatArray,
        scores: FloatArray,
        numDetections: Int
    ): List<DetectionResult> {
        val results = mutableListOf<DetectionResult>()

        val detectionsToProcess = numDetections.coerceAtMost(ObjectDetector.MAX_DETECTIONS)

        for (i in 0 until detectionsToProcess) {
            val score = scores[i]

            // Filter by confidence
            if (score < confidenceThreshold) continue

            val classIndex = classes[i].toInt()
            if (classIndex < 0 || classIndex >= labels.size) continue

            // Extract bounding box (model outputs: ymin, xmin, ymax, xmax)
            val location = locations[i]
            val boundingBox = BoundingBox(
                left = location[1].coerceIn(0f, 1f),   // xmin
                top = location[0].coerceIn(0f, 1f),    // ymin
                right = location[3].coerceIn(0f, 1f),  // xmax
                bottom = location[2].coerceIn(0f, 1f)  // ymax
            )

            if (!boundingBox.isValid()) continue

            results.add(
                DetectionResult(
                    label = labels[classIndex],
                    confidence = score,
                    boundingBox = boundingBox
                )
            )
        }

        return results
    }

    override fun isReady(): Boolean {
        return interpreter != null && labels.isNotEmpty()
    }

    override fun close() {
        Log.d(TAG, "Closing TFLiteObjectDetector")

        // Watch scope for leaks before cancelling
        LeakCanaryWatchers.watchCoroutineScope(detectorScope, "TFLiteDetector.detectorScope")

        // Cancel all ongoing coroutines and Flow emissions
        detectorScope.cancel()

        // Close TFLite resources
        interpreter?.close()
        interpreter = null

        gpuDelegate?.close()
        gpuDelegate = null

        inputImageBuffer = null

        Log.d(TAG, "TFLiteObjectDetector closed successfully")
    }
}