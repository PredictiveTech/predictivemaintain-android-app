package pe.edu.upc.predictivemaintain.app.telemetry.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import pe.edu.upc.predictivemaintain.app.R
import pe.edu.upc.predictivemaintain.app.core.ui.EmptyStateComponent
import pe.edu.upc.predictivemaintain.app.core.ui.LoadingComponent
import pe.edu.upc.predictivemaintain.app.core.ui.StatusChip
import pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject.SensorMetric
import pe.edu.upc.predictivemaintain.app.telemetry.presentation.viewmodel.SensorPanelViewModel
import java.time.Duration
import java.time.Instant

@Composable
fun SensorPanel(
    assetId: String,
    viewModel: SensorPanelViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(assetId, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                viewModel.loadSensors(assetId)
                delay(15000L) // 15 seconds
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.sensor_panel_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )

        uiState.errorMessage?.let { errorMsg ->
            Text(
                text = errorMsg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        if (uiState.isLoading && uiState.items.isEmpty()) {
            LoadingComponent()
        } else if (uiState.items.isEmpty()) {
            EmptyStateComponent(message = stringResource(R.string.reading_none))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                uiState.items.forEach { sensor ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val metricName = when (sensor.metric) {
                                    SensorMetric.VIBRATION -> stringResource(R.string.metric_vibration)
                                    SensorMetric.TEMPERATURE -> stringResource(R.string.metric_temperature)
                                    SensorMetric.PRESSURE -> stringResource(R.string.metric_pressure)
                                    SensorMetric.CURRENT -> stringResource(R.string.metric_current)
                                    SensorMetric.NOISE -> stringResource(R.string.metric_noise)
                                    SensorMetric.UNKNOWN -> sensor.metric.name
                                }
                                Text(
                                    text = metricName,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f)
                                )
                                StatusChip(text = sensor.communication.name)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            val readingText = if (sensor.latestReading != null) {
                                "${sensor.latestReading.value} ${sensor.unit}"
                            } else {
                                stringResource(R.string.reading_none)
                            }
                            Text(
                                text = readingText,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            if (sensor.threshold != null) {
                                Text(
                                    text = stringResource(
                                        R.string.threshold_range,
                                        sensor.threshold.lowerBound,
                                        sensor.threshold.upperBound,
                                        sensor.unit
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusChip(text = sensor.rangeStatus.name)
                                Spacer(modifier = Modifier.weight(1f))
                                if (sensor.latestReading != null) {
                                    val ageText = formatAge(sensor.latestReading.measuredAt)
                                    Text(
                                        text = ageText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun formatAge(measuredAt: Instant): String {
    val now = Instant.now()
    val seconds = Duration.between(measuredAt, now).seconds
    return when {
        seconds < 60 -> stringResource(R.string.reading_ago, maxOf(0L, seconds))
        seconds < 3600 -> stringResource(R.string.reading_ago_minutes, seconds / 60)
        else -> stringResource(R.string.reading_ago_hours, seconds / 3600)
    }
}
