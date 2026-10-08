package pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.predictivemaintain.app.R
import pe.edu.upc.predictivemaintain.app.core.ui.EmptyStateComponent
import pe.edu.upc.predictivemaintain.app.core.ui.LoadingComponent
import pe.edu.upc.predictivemaintain.app.core.ui.StatusChip
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel.AlertsViewModel
import java.time.Duration
import java.time.Instant

/** A status filter: the value sent to the ViewModel (null means "All") and the label shown on the chip. */
private data class StatusFilterOption(val value: String?, @StringRes val labelRes: Int)

private val statusFilters = listOf(
    StatusFilterOption("IN_REVIEW", R.string.filter_status_in_review),
    StatusFilterOption("CONFIRMED", R.string.filter_status_confirmed),
    StatusFilterOption("DISCARDED", R.string.filter_status_discarded),
    StatusFilterOption("RESOLVED", R.string.filter_status_resolved),
    StatusFilterOption(null, R.string.filter_all)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel,
    onAlertClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    assetId: String? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // The asset filter arrives from navigation as raw text; the ViewModel decides whether it is valid
    LaunchedEffect(assetId) {
        viewModel.setAssetIdArg(assetId)
    }

    // Every time the user comes back to this screen the list is refreshed (there is no push notification yet)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = stringResource(R.string.alerts_title)) })
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // One scrollable row: the five filters do not fit on a phone screen
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(statusFilters) { option ->
                    FilterChip(
                        selected = uiState.selectedStatusFilter == option.value,
                        onClick = { viewModel.setStatusFilter(option.value) },
                        label = {
                            Text(
                                text = stringResource(option.labelRes),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    )
                }
            }

            // When there are items on screen, a failed refresh is shown above them and the items stay
            if (uiState.items.isNotEmpty()) {
                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val errorMessage = uiState.errorMessage
                when {
                    uiState.isLoading && uiState.items.isEmpty() -> LoadingComponent()

                    // A failed request with nothing to show is an ERROR, not "no alerts"
                    uiState.items.isEmpty() && errorMessage != null -> AlertsErrorState(
                        message = errorMessage,
                        onRetry = { viewModel.refresh() }
                    )

                    uiState.items.isEmpty() -> EmptyStateComponent(
                        message = stringResource(R.string.no_alerts)
                    )

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.items, key = { it.id.value }) { alert ->
                            AlertCard(
                                assetLabel = "${alert.assetCode} - ${alert.assetName}",
                                severityLabel = severityLabel(alert.severity.name),
                                statusLabel = statusLabel(alert.status.name),
                                observedText = alert.diagnostic?.let { diagnostic ->
                                    stringResource(
                                        R.string.alert_observed_value,
                                        diagnostic.observedValue,
                                        diagnostic.unit
                                    )
                                },
                                rangeText = alert.diagnostic?.let { diagnostic ->
                                    stringResource(
                                        R.string.threshold_range,
                                        diagnostic.lowerBound,
                                        diagnostic.upperBound,
                                        diagnostic.unit
                                    )
                                },
                                ageText = alertAgeText(alert.raisedAt),
                                onClick = { onAlertClick(alert.id.value) }
                            )
                        }

                        if (uiState.hasMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Button(onClick = { viewModel.loadMore() }) {
                                        Text(text = stringResource(R.string.load_more))
                                    }
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
private fun AlertCard(
    assetLabel: String,
    severityLabel: String,
    statusLabel: String,
    observedText: String?,
    rangeText: String?,
    ageText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = assetLabel,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(text = severityLabel)
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (observedText != null) {
                Text(
                    text = observedText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (rangeText != null) {
                Text(
                    text = rangeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(text = statusLabel)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = ageText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AlertsErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.alerts_retry))
        }
    }
}

@Composable
private fun severityLabel(severity: String): String = when (severity) {
    "CRITICAL" -> stringResource(R.string.alerts_severity_critical)
    "WARNING" -> stringResource(R.string.alerts_severity_warning)
    else -> stringResource(R.string.alerts_severity_unknown)
}

@Composable
private fun statusLabel(status: String): String = when (status) {
    "IN_REVIEW" -> stringResource(R.string.filter_status_in_review)
    "CONFIRMED" -> stringResource(R.string.filter_status_confirmed)
    "DISCARDED" -> stringResource(R.string.filter_status_discarded)
    "RESOLVED" -> stringResource(R.string.filter_status_resolved)
    else -> stringResource(R.string.alerts_status_unknown)
}

/** How long ago an alert was raised, split from the text so it can be tested without Android. */
internal sealed interface AlertAge {
    data object JustNow : AlertAge
    data class Minutes(val value: Long) : AlertAge
    data class Hours(val value: Long) : AlertAge
    data class Days(val value: Long) : AlertAge
}

internal fun alertAgeOf(raisedAt: Instant, now: Instant): AlertAge {
    val elapsed = Duration.between(raisedAt, now)
    return when {
        // A phone whose clock is behind the server's would give a negative age
        elapsed.isNegative || elapsed.seconds < 60 -> AlertAge.JustNow
        elapsed.toMinutes() < 60 -> AlertAge.Minutes(elapsed.toMinutes())
        elapsed.toHours() < 24 -> AlertAge.Hours(elapsed.toHours())
        else -> AlertAge.Days(elapsed.toDays())
    }
}

@Composable
private fun alertAgeText(raisedAt: Instant): String =
    when (val age = alertAgeOf(raisedAt, Instant.now())) {
        AlertAge.JustNow -> stringResource(R.string.alerts_age_now)
        is AlertAge.Minutes -> stringResource(R.string.alerts_age_minutes, age.value)
        is AlertAge.Hours -> stringResource(R.string.alerts_age_hours, age.value)
        is AlertAge.Days -> stringResource(R.string.alerts_age_days, age.value)
    }