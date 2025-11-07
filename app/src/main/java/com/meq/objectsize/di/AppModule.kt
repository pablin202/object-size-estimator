package com.meq.objectsize.di

import android.content.Context
import com.meq.objectsize.domain.PerformanceMonitor
import com.meq.objectsize.domain.ProfilerHelper
import com.meq.objectsize.domain.SizeCalculator
import com.meq.objectsize.ml.ObjectDetector
import com.meq.objectsize.ml.TFLiteObjectDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for dependency injection
 *
 * Provides:
 * - ObjectDetector (TFLite implementation)
 * - SizeCalculator (domain logic)
 *
 * Note: ImageAnalyzer and CameraManager use @Inject constructor
 * so they don't need @Provides methods
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provide ObjectDetector singleton
     *
     * Singleton because:
     * - TFLite model is expensive to load
     * - Want to reuse across screen rotations
     *
     * Uses @Provides because ObjectDetector is an interface
     * and we need to specify the implementation
     */
    @Provides
    @Singleton
    fun provideObjectDetector(
        @ApplicationContext context: Context,
        performanceMonitor: PerformanceMonitor,
        profilerHelper: ProfilerHelper
    ): ObjectDetector {
        return TFLiteObjectDetector(
            context = context,
            useGpu = true,  // GPU works with 2.16.1 when tensorflow-lite-gpu-api is explicitly added
            performanceMonitor = performanceMonitor,
            profilerHelper = profilerHelper
        )
    }

    /**
     * Provide SizeCalculator
     *
     * Singleton because it's stateless
     */
    @Provides
    @Singleton
    fun provideSizeCalculator(): SizeCalculator {
        return SizeCalculator()
    }

    /**
     * Provide PerformanceMonitor
     *
     * Singleton because it maintains state across detections
     */
    @Provides
    @Singleton
    fun providePerformanceMonitor(): PerformanceMonitor {
        return PerformanceMonitor()
    }

    /**
     * Provide ProfilerHelper
     *
     * Note: ProfilerHelper uses constructor injection with @Inject
     * so this @Provides method is not strictly necessary, but included
     * for consistency and explicit dependency declaration
     *
     * Singleton because:
     * - Manages shared file system resources
     * - Coordinates snapshots across app lifecycle
     */
    @Provides
    @Singleton
    fun provideProfilerHelper(
        @ApplicationContext context: Context,
        performanceMonitor: PerformanceMonitor
    ): ProfilerHelper {
        return ProfilerHelper(context, performanceMonitor)
    }
}