package com.meq.objectsize.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Settings screen for the app
 *
 * Displays all configurable settings grouped by category.
 * Uses Material3 components and reactive StateFlow updates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Detection Settings
            SettingsSection(title = "Detection Settings") {
                SliderSetting(
                    label = "Confidence Threshold",
                    value = settings.confidenceThreshold,
                    valueRange = 0.1f..0.9f,
                    onValueChange = viewModel::updateConfidenceThreshold,
                    valueFormat = { "%.2f".format(it) },
                    description = "Minimum confidence score for object detection"
                )

                IntSliderSetting(
                    label = "Max Objects",
                    value = settings.maxObjects,
                    valueRange = 1..20,
                    onValueChange = viewModel::updateMaxObjects,
                    description = "Maximum number of objects to detect"
                )

                SliderSetting(
                    label = "Same Plane Threshold",
                    value = settings.samePlaneThreshold,
                    valueRange = 0.05f..0.50f,
                    onValueChange = viewModel::updateSamePlaneThreshold,
                    valueFormat = { "${(it * 100).toInt()}%" },
                    description = "Percentage threshold for same plane detection"
                )
            }

            // Camera Settings
            SettingsSection(title = "Camera Settings") {
                ResolutionSetting(
                    width = settings.targetResolutionWidth,
                    height = settings.targetResolutionHeight,
                    onResolutionChange = viewModel::updateTargetResolution
                )

                IntSliderSetting(
                    label = "Frame Capture Interval (ms)",
                    value = settings.frameCaptureIntervalMs.toInt(),
                    valueRange = 33..500,
                    step = 10,
                    onValueChange = { viewModel.updateFrameCaptureInterval(it.toLong()) },
                    description = "Time between frame captures (33ms = 30 FPS, 100ms = 10 FPS)"
                )
            }

            // ML Settings
            SettingsSection(title = "Machine Learning") {
                SwitchSetting(
                    label = "GPU Acceleration",
                    checked = settings.enableGpuDelegate,
                    onCheckedChange = viewModel::updateEnableGpuDelegate,
                    description = "Use GPU for faster inference (if available)"
                )

                IntSliderSetting(
                    label = "CPU Threads",
                    value = settings.numThreads,
                    valueRange = 1..8,
                    onValueChange = viewModel::updateNumThreads,
                    description = "Number of CPU threads for ML processing"
                )
            }

            // Performance Settings
            SettingsSection(title = "Performance") {
                SwitchSetting(
                    label = "Performance Overlay",
                    checked = settings.showPerformanceOverlay,
                    onCheckedChange = viewModel::updateShowPerformanceOverlay,
                    description = "Show FPS and inference time on screen"
                )

                IntSliderSetting(
                    label = "Refresh Rate (ms)",
                    value = settings.performanceRefreshRate.toInt(),
                    valueRange = 100..5000,
                    step = 100,
                    onValueChange = { viewModel.updatePerformanceRefreshRate(it.toLong()) },
                    description = "Performance metrics update interval"
                )
            }

            // Reference Object Sizes
            SettingsSection(title = "Reference Object Sizes (cm)") {
                ObjectSizeSetting(
                    label = "Cell Phone",
                    width = settings.cellPhoneWidth,
                    height = settings.cellPhoneHeight,
                    onSizeChange = viewModel::updateCellPhoneSize
                )

                ObjectSizeSetting(
                    label = "Book",
                    width = settings.bookWidth,
                    height = settings.bookHeight,
                    onSizeChange = viewModel::updateBookSize
                )

                ObjectSizeSetting(
                    label = "Bottle",
                    width = settings.bottleWidth,
                    height = settings.bottleHeight,
                    onSizeChange = viewModel::updateBottleSize
                )

                ObjectSizeSetting(
                    label = "Cup",
                    width = settings.cupWidth,
                    height = settings.cupHeight,
                    onSizeChange = viewModel::updateCupSize
                )

                ObjectSizeSetting(
                    label = "Keyboard",
                    width = settings.keyboardWidth,
                    height = settings.keyboardHeight,
                    onSizeChange = viewModel::updateKeyboardSize
                )
            }

            // Reset Button
            Button(
                onClick = viewModel::resetToDefaults,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Reset to Defaults")
            }

            // Bottom spacing
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider()
        content()
    }
}

@Composable
private fun SwitchSetting(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SliderSetting(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    valueFormat: (Float) -> String = { "%.2f".format(it) },
    description: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = valueFormat(value),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}

@Composable
private fun IntSliderSetting(
    label: String,
    value: Int,
    valueRange: IntRange,
    onValueChange: (Int) -> Unit,
    step: Int = 1,
    description: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            steps = (valueRange.last - valueRange.first) / step - 1
        )
    }
}

@Composable
private fun ResolutionSetting(
    width: Int,
    height: Int,
    onResolutionChange: (Int, Int) -> Unit
) {
    var selectedResolution by remember(width, height) {
        mutableStateOf("${width}x${height}")
    }

    Column {
        Text(
            text = "Target Resolution",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Camera preview resolution",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        val resolutions = listOf(
            "640x480" to Pair(640, 480),
            "1280x720" to Pair(1280, 720),
            "1920x1080" to Pair(1920, 1080)
        )

        resolutions.forEach { (label, resolution) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedResolution == label,
                    onClick = {
                        selectedResolution = label
                        onResolutionChange(resolution.first, resolution.second)
                    }
                )
                Text(
                    text = label,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ObjectSizeSetting(
    label: String,
    width: Float,
    height: Float,
    onSizeChange: (Float, Float) -> Unit
) {
    var widthText by remember(width) { mutableStateOf(width.toString()) }
    var heightText by remember(height) { mutableStateOf(height.toString()) }

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = widthText,
                onValueChange = { newValue ->
                    widthText = newValue
                    newValue.toFloatOrNull()?.let { w ->
                        onSizeChange(w, height)
                    }
                },
                label = { Text("Width") },
                suffix = { Text("cm") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = heightText,
                onValueChange = { newValue ->
                    heightText = newValue
                    newValue.toFloatOrNull()?.let { h ->
                        onSizeChange(width, h)
                    }
                },
                label = { Text("Height") },
                suffix = { Text("cm") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
    }
}
