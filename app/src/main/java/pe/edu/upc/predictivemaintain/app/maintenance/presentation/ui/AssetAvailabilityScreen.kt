package pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.predictivemaintain.app.R
import pe.edu.upc.predictivemaintain.app.core.ui.AccentCard
import pe.edu.upc.predictivemaintain.app.core.ui.ColoredStatusChip
import pe.edu.upc.predictivemaintain.app.core.ui.EmptyStateComponent
import pe.edu.upc.predictivemaintain.app.core.ui.LoadingComponent
import pe.edu.upc.predictivemaintain.app.core.ui.StatusTone
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetEvent
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertSeverity
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel.AssetAvailabilityViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val dateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

private fun Instant.display(): String = dateTimeFormatter.format(atZone(ZoneId.systemDefault()))

/** US-14: failure history and availability of one asset. US-16: export action. Manager only. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetAvailabilityScreen(
    viewModel: AssetAvailabilityViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ShareExportedReportEffect(
        exported = uiState.exported,
        snackbarHostState = snackbarHostState,
        onShared = viewModel::onExportShared
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.report_availability_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.report_back)
                        )
                    }
                },
                actions = {
                    if (uiState.isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = viewModel::export) {
                            Icon(
                                imageVector = Icons.Filled.FileDownload,
                                contentDescription = stringResource(R.string.report_export)
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                DateRangeSelector(
                    range = uiState.range,
                    onRangeChange = viewModel::onRangeChange,
                    enabled = !uiState.isLoading
                )
            }
            uiState.rangeError?.let { error ->
                item { ErrorText(error.message()) }
            }
            uiState.errorMessage?.let { message ->
                item {
                    ErrorText(message)
                    TextButton(onClick = viewModel::load) { Text(text = stringResource(R.string.retry)) }
                }
            }
            uiState.exportError?.let { message ->
                item {
                    ErrorText(message)
                    TextButton(onClick = viewModel::dismissExportError) { Text(text = stringResource(R.string.cancel)) }
                }
            }

            if (uiState.isLoading && uiState.report == null) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            uiState.report?.let { report ->
                item {
                    val tone = availabilityTone(report.availabilityPercent)
                    AccentCard(
                        accent = tone.accent,
                        icon = Icons.Filled.Assessment,
                        title = formatPercent(report.availabilityPercent),
                        subtitle = report.assetCode,
                        onClick = null,
                        trailing = {
                            ColoredStatusChip(
                                text = stringResource(R.string.report_availability_label),
                                tone = tone
                            )
                        }
                    ) {
                        SummaryLine(stringResource(R.string.report_total_downtime), formatDuration(report.downtimeSeconds))
                        SummaryLine(stringResource(R.string.report_period_analysed), formatDuration(report.periodSeconds))
                    }
                }

                item { SectionTitle(stringResource(R.string.report_downtime_intervals)) }
                if (report.downtimeIntervals.isEmpty()) {
                    item { MutedText(stringResource(R.string.report_no_downtime)) }
                } else {
                    items(report.downtimeIntervals, key = { "d-" + it.referenceId + it.occurredAt }) { DowntimeCard(it) }
                }

                item { SectionTitle(stringResource(R.string.report_alerts_section)) }
                if (report.alerts.isEmpty()) {
                    item { MutedText(stringResource(R.string.report_no_alerts)) }
                } else {
                    items(report.alerts, key = { "a-" + it.referenceId + it.occurredAt }) { AlertEventCard(it) }
                }
            }
        }
    }
}

@Composable
private fun DowntimeCard(event: AssetEvent) {
    val ended = event.endedAt
    AccentCard(
        accent = StatusTone.DANGER.accent,
        icon = Icons.Filled.PowerOff,
        title = ended?.let { formatDuration(java.time.Duration.between(event.occurredAt, it).seconds.coerceAtLeast(0)) }
            ?: stringResource(R.string.report_ongoing),
        subtitle = stringResource(
            R.string.report_interval,
            event.occurredAt.display(),
            ended?.display() ?: stringResource(R.string.report_ongoing)
        ),
        onClick = null,
        trailing = {
            if (ended == null) ColoredStatusChip(text = stringResource(R.string.report_ongoing), tone = StatusTone.DANGER)
        }
    )
}

@Composable
private fun AlertEventCard(event: AssetEvent) {
    val severity = AlertSeverity.fromString(event.severity)
    val tone = severityTone(severity)
    AccentCard(
        accent = tone.accent,
        icon = Icons.Filled.NotificationsActive,
        title = event.occurredAt.display(),
        subtitle = event.status.orEmpty(),
        onClick = null,
        trailing = {
            ColoredStatusChip(
                text = stringResource(
                    when (severity) {
                        AlertSeverity.CRITICAL -> R.string.severity_critical
                        AlertSeverity.WARNING -> R.string.severity_warning
                        AlertSeverity.UNKNOWN -> R.string.alerts_severity_unknown
                    }
                ),
                tone = tone
            )
        }
    )
}

@Composable
internal fun SummaryLine(label: String, value: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
internal fun MutedText(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
internal fun ErrorText(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
}
