package pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.predictivemaintain.app.R
import pe.edu.upc.predictivemaintain.app.core.ui.LoadingComponent
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.DeactivateDialog
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel.AssetDetailViewModel
import pe.edu.upc.predictivemaintain.app.telemetry.presentation.ui.SensorPanel
import pe.edu.upc.predictivemaintain.app.telemetry.presentation.viewmodel.SensorPanelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDetailScreen(
    assetId: String,
    modifier: Modifier = Modifier,
    viewModel: AssetDetailViewModel = hiltViewModel(),
    sensorPanelViewModel: SensorPanelViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onViewAlertsClick: (String) -> Unit,
    canManage: Boolean = false,
    onEditClick: () -> Unit = {},
    onDeactivated: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(assetId) {
        viewModel.loadAsset(assetId)
    }

    LaunchedEffect(uiState.deactivated) {
        if (uiState.deactivated) onDeactivated()
    }

    DeactivateDialogs(
        dialog = uiState.deactivateDialog,
        onConfirm = viewModel::confirmDeactivate,
        onDismiss = viewModel::dismissDeactivateDialog
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = uiState.asset?.name ?: stringResource(R.string.assets_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text(text = "←", style = MaterialTheme.typography.titleMedium)
                    }
                },
                actions = {
                    // An inactive asset cannot be edited (the backend answers 409).
                    if (canManage && uiState.asset?.active == true) {
                        TextButton(onClick = onEditClick) {
                            Text(text = stringResource(R.string.edit_asset_action))
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        val scrollState = rememberScrollState()

        if (uiState.isLoading && uiState.asset == null) {
            LoadingComponent(modifier = Modifier.padding(innerPadding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
            ) {
                uiState.errorMessage?.let { errorMsg ->
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                uiState.asset?.let { asset ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "${asset.code.value} - ${asset.name}",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.asset_type, asset.assetType),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (!asset.location.isNullOrBlank()) {
                            Text(
                                text = stringResource(R.string.asset_location, asset.location),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            text = stringResource(R.string.asset_criticality, asset.criticality.name),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (!asset.active) {
                        Text(
                            text = stringResource(R.string.asset_inactive_notice),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { onViewAlertsClick(assetId) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(text = stringResource(R.string.view_asset_alerts))
                    }

                    if (canManage && asset.active) {
                        uiState.deactivateError?.let { error ->
                            Text(
                                text = submitErrorText(error),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                        OutlinedButton(
                            onClick = viewModel::requestDeactivate,
                            enabled = !uiState.isDeactivating,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            if (uiState.isDeactivating) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Text(text = stringResource(R.string.deactivate_asset_action))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SensorPanel(
                        assetId = assetId,
                        viewModel = sensorPanelViewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun DeactivateDialogs(
    dialog: DeactivateDialog,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (dialog == DeactivateDialog.NONE) return
    val message = if (dialog == DeactivateDialog.CONFIRM_WITH_OPEN_ORDERS) {
        R.string.deactivate_asset_open_orders_message
    } else {
        R.string.deactivate_asset_confirm_message
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.deactivate_asset_confirm_title)) },
        text = { Text(text = stringResource(message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.deactivate_asset_action),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.cancel)) }
        }
    )
}
