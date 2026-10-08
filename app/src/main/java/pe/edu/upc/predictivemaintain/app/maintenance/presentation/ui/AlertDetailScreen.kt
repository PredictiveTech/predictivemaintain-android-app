package pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.predictivemaintain.app.R
import pe.edu.upc.predictivemaintain.app.core.ui.LoadingComponent
import pe.edu.upc.predictivemaintain.app.core.ui.StatusChip
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel.AlertDetailUiEvent
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel.AlertDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDetailScreen(
    alertId: String,
    viewModel: AlertDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNavigateToWorkOrder: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDiscardDialog by remember { mutableStateOf(false) }
    var discardReason by remember { mutableStateOf("") }

    LaunchedEffect(alertId) {
        viewModel.loadAlert(alertId)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AlertDetailUiEvent.NavigateToWorkOrder -> onNavigateToWorkOrder(event.workOrderId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.alerts_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text(text = "←", style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        val scrollState = rememberScrollState()

        if (uiState.isLoading && uiState.alert == null) {
            LoadingComponent(modifier = Modifier.padding(innerPadding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                uiState.errorMessage?.let { errorMsg ->
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                uiState.successMessage?.let { successMsg ->
                    Text(
                        text = successMsg,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                uiState.alert?.let { alert ->
                    Text(
                        text = "${alert.assetCode} - ${alert.assetName}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusChip(text = alert.severity.name)
                        StatusChip(text = alert.status.name)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (alert.diagnostic != null) {
                        Text(
                            text = stringResource(R.string.alert_diagnostic_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(
                                R.string.alert_observed_value,
                                alert.diagnostic.observedValue,
                                alert.diagnostic.unit
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(
                                R.string.threshold_range,
                                alert.diagnostic.lowerBound,
                                alert.diagnostic.upperBound,
                                alert.diagnostic.unit
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!alert.discardReason.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "${stringResource(R.string.alert_discard)}: ${alert.discardReason}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    if (uiState.canReview) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { viewModel.confirmAlert() },
                                enabled = !uiState.isSubmitting,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = stringResource(R.string.alert_confirm))
                            }
                            Button(
                                onClick = { showDiscardDialog = true },
                                enabled = !uiState.isSubmitting,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = stringResource(R.string.alert_discard))
                            }
                        }
                    }

                    if (uiState.canCreateWorkOrder) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.createWorkOrder() },
                            enabled = !uiState.isSubmitting,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = stringResource(R.string.orders_create_button))
                        }
                    }
                }
            }
        }

        if (showDiscardDialog) {
            AlertDialog(
                onDismissRequest = { showDiscardDialog = false },
                title = { Text(text = stringResource(R.string.alert_discard_reason_title)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = discardReason,
                            onValueChange = { discardReason = it },
                            label = { Text(text = stringResource(R.string.alert_discard_reason_hint)) },
                            isError = discardReason.isBlank() || discardReason.length > 500,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (discardReason.isBlank()) {
                            Text(
                                text = stringResource(R.string.error_discard_reason_blank),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else if (discardReason.length > 500) {
                            Text(
                                text = stringResource(R.string.error_discard_reason_too_long),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val reason = discardReason
                            showDiscardDialog = false
                            discardReason = ""
                            viewModel.discardAlert(reason)
                        },
                        enabled = discardReason.isNotBlank() && discardReason.length <= 500
                    ) {
                        Text(text = stringResource(R.string.alert_submit))
                    }
                },
                dismissButton = {
                    Button(onClick = { showDiscardDialog = false }) {
                        Text(text = stringResource(R.string.alert_cancel))
                    }
                }
            )
        }
    }
}
