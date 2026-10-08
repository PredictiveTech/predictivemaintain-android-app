package pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
    onViewAlertsClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(assetId) {
        viewModel.loadAsset(assetId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = uiState.asset?.name ?: stringResource(R.string.assets_title)) },
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

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { onViewAlertsClick(assetId) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(text = stringResource(R.string.view_asset_alerts))
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
