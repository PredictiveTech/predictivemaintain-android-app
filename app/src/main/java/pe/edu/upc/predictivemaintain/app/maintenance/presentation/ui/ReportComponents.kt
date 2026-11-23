package pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.predictivemaintain.app.R
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.ExportedReport
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.DateRange
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.ReportRangeError
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

internal fun LocalDate.display(): String = dateFormatter.format(this)

@Composable
internal fun ReportRangeError.message(): String = stringResource(
    when (this) {
        ReportRangeError.START_AFTER_END -> R.string.report_error_start_after_end
        ReportRangeError.STARTS_IN_FUTURE -> R.string.report_error_future
        ReportRangeError.TOO_LONG -> R.string.report_error_too_long
    }
)

/** Card that shows the chosen days and opens a date range picker (days after today cannot be chosen). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateRangeSelector(
    range: DateRange,
    onRangeChange: (DateRange) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var showPicker by remember { mutableStateOf(false) }

    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.report_range_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.report_range_value, range.start.display(), range.end.display()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            TextButton(onClick = { showPicker = true }, enabled = enabled) {
                Text(text = stringResource(R.string.report_range_change))
            }
        }
    }

    if (showPicker) {
        val todayEndUtcMillis = remember {
            LocalDate.now().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() - 1
        }
        val state = rememberDateRangePickerState(
            initialSelectedStartDateMillis = range.start.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            initialSelectedEndDateMillis = range.end.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayEndUtcMillis
            }
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    enabled = state.selectedStartDateMillis != null,
                    onClick = {
                        val start = state.selectedStartDateMillis?.toUtcDate()
                        if (start != null) {
                            // Picking a single day selects only that day.
                            onRangeChange(DateRange(start, state.selectedEndDateMillis?.toUtcDate() ?: start))
                        }
                        showPicker = false
                    }
                ) { Text(text = stringResource(R.string.report_range_apply)) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text(text = stringResource(R.string.cancel)) }
            }
        ) {
            DateRangePicker(state = state, modifier = Modifier.weight(1f, fill = false))
        }
    }
}

private fun Long.toUtcDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

/**
 * US-16. When a report was saved, tells the user where and opens the system share sheet for it.
 * [onShared] runs right after, so the event is not repeated on recomposition.
 */
@Composable
internal fun ShareExportedReportEffect(
    exported: ExportedReport?,
    snackbarHostState: SnackbarHostState,
    onShared: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val savedMessage = exported?.let { stringResource(R.string.report_export_saved, it.fileName) }
    val shareTitle = stringResource(R.string.report_share_title)

    LaunchedEffect(exported) {
        if (exported == null) return@LaunchedEffect
        val uri = Uri.parse(exported.uri)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = exported.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, exported.fileName)
            // The chooser needs the ClipData to pass the read grant on to the app the user picks.
            clipData = ClipData.newRawUri(exported.fileName, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(Intent.createChooser(send, shareTitle))
        } catch (_: ActivityNotFoundException) {
            // No app can receive it; the file is still in Downloads.
        }
        // Launched on its own scope: onShared() clears [exported], which restarts this effect.
        if (savedMessage != null) scope.launch { snackbarHostState.showSnackbar(savedMessage) }
        onShared()
    }
}
