package pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui

import pe.edu.upc.predictivemaintain.app.core.ui.ScreenHeader
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.clickable
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Asset
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import pe.edu.upc.predictivemaintain.app.core.ui.AccentCard
import pe.edu.upc.predictivemaintain.app.core.ui.ColoredStatusChip
import pe.edu.upc.predictivemaintain.app.core.ui.StatusTone
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetStatus
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.predictivemaintain.app.R
import pe.edu.upc.predictivemaintain.app.core.ui.EmptyStateComponent
import pe.edu.upc.predictivemaintain.app.core.ui.LoadingComponent
import pe.edu.upc.predictivemaintain.app.core.ui.StatusChip
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel.AssetsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsScreen(
    viewModel: AssetsViewModel,
    onAssetClick: (String) -> Unit,
    canCreate: Boolean = false,
    onCreateClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            ScreenHeader(title = stringResource(R.string.assets_title), icon = Icons.Filled.Settings)
        },
        floatingActionButton = {
            if (canCreate) {
                FloatingActionButton(onClick = onCreateClick) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.create_asset_title)
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedStatusFilter == null,
                    onClick = { viewModel.setStatusFilter(null) },
                    label = { Text(text = stringResource(R.string.filter_all)) }
                )
                FilterChip(
                    selected = uiState.selectedStatusFilter == "OPERATIONAL",
                    onClick = { viewModel.setStatusFilter("OPERATIONAL") },
                    label = { Text(text = stringResource(R.string.filter_operational)) }
                )
                FilterChip(
                    selected = uiState.selectedStatusFilter == "IN_ALERT",
                    onClick = { viewModel.setStatusFilter("IN_ALERT") },
                    label = { Text(text = stringResource(R.string.filter_in_alert)) }
                )
                FilterChip(
                    selected = uiState.selectedStatusFilter == "NO_COMMUNICATION",
                    onClick = { viewModel.setStatusFilter("NO_COMMUNICATION") },
                    label = { Text(text = stringResource(R.string.filter_no_communication_short)) }
                )
            }

            uiState.errorMessage?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (uiState.isLoading && uiState.items.isEmpty()) {
                    LoadingComponent()
                } else if (uiState.items.isEmpty()) {
                    EmptyStateComponent(message = stringResource(R.string.no_assets))
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.items, key = { it.id.value }) { asset ->
                            AssetCard(asset = asset, onClick = { onAssetClick(asset.id.value) })
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
private fun AssetCard(asset: Asset, onClick: () -> Unit) {
    AccentCard(
        accent = assetStatusTone(asset.status).accent,
        icon = assetTypeIcon(asset.assetType),
        title = asset.name,
        subtitle = asset.code.value,
        onClick = onClick,
        trailing = { AssetStatusChip(status = asset.status) }
    ) {
        Text(
            text = stringResource(R.string.asset_type, asset.assetType),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!asset.location.isNullOrBlank()) {
            Text(
                text = stringResource(R.string.asset_location, asset.location),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AssetStatusChip(status: AssetStatus) {
    val (label, tone) = when (status) {
        AssetStatus.IN_ALERT -> R.string.status_in_alert to StatusTone.DANGER
        AssetStatus.OPERATIONAL -> R.string.status_operational to StatusTone.SUCCESS
        AssetStatus.NO_COMMUNICATION -> R.string.status_no_communication to StatusTone.INFO
        AssetStatus.INACTIVE -> R.string.status_inactive to StatusTone.NEUTRAL
        AssetStatus.UNKNOWN -> R.string.status_unknown to StatusTone.NEUTRAL
    }
    ColoredStatusChip(text = stringResource(label), tone = tone)
}
