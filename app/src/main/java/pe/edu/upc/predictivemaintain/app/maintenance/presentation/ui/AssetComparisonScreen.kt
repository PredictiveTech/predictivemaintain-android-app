package pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.predictivemaintain.app.R
import pe.edu.upc.predictivemaintain.app.core.ui.ColoredStatusChip
import pe.edu.upc.predictivemaintain.app.core.ui.LoadingComponent
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.CompareAssetsUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Asset
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetComparisonRow
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.ComparisonSort
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel.AssetComparisonViewModel

/** US-15: compare several assets side by side. US-16: export action. Manager only. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetComparisonScreen(
    viewModel: AssetComparisonViewModel,
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
                title = { Text(text = stringResource(R.string.compare_title)) },
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
                        IconButton(onClick = viewModel::export, enabled = uiState.canRun) {
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
                DateRangeSelector(range = uiState.range, onRangeChange = viewModel::onRangeChange)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            R.string.compare_select_assets,
                            uiState.selectedIds.size,
                            CompareAssetsUseCase.MAX_ASSETS
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = viewModel::clearSelection, enabled = uiState.selectedIds.isNotEmpty()) {
                        Text(text = stringResource(R.string.compare_clear))
                    }
                }
            }
            if (uiState.maxSelectionReached) {
                item { ErrorText(stringResource(R.string.compare_max_reached, CompareAssetsUseCase.MAX_ASSETS)) }
            }
            uiState.assetsError?.let { message ->
                item {
                    ErrorText(message)
                    TextButton(onClick = viewModel::loadAssets) { Text(text = stringResource(R.string.retry)) }
                }
            }
            if (uiState.isLoadingAssets) {
                item { LoadingComponent() }
            } else if (uiState.assets.isEmpty() && uiState.assetsError == null) {
                item { MutedText(stringResource(R.string.compare_no_assets)) }
            } else {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            uiState.assets.forEachIndexed { index, asset ->
                                if (index > 0) HorizontalDivider()
                                AssetCheckRow(
                                    asset = asset,
                                    checked = asset.id.value in uiState.selectedIds,
                                    onToggle = { viewModel.toggleAsset(asset.id.value) }
                                )
                            }
                        }
                    }
                }
            }

            uiState.rangeError?.let { item { ErrorText(it.message()) } }
            uiState.exportError?.let { message ->
                item {
                    ErrorText(message)
                    TextButton(onClick = viewModel::dismissExportError) { Text(text = stringResource(R.string.cancel)) }
                }
            }

            item {
                Button(
                    onClick = viewModel::compare,
                    enabled = uiState.canRun,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isComparing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(text = stringResource(R.string.compare_run))
                    }
                }
            }

            uiState.errorMessage?.let { item { ErrorText(it) } }

            val rows = uiState.rows
            if (rows == null) {
                if (uiState.selectedIds.isEmpty()) item { MutedText(stringResource(R.string.compare_hint_select)) }
            } else {
                item { SectionTitle(stringResource(R.string.compare_sort_by)) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        // Wrap on narrow screens by splitting in two rows of chips.
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SortChip(ComparisonSort.AVAILABILITY, R.string.compare_sort_availability, uiState.sort, viewModel::onSortChange)
                                SortChip(ComparisonSort.DOWNTIME, R.string.compare_sort_downtime, uiState.sort, viewModel::onSortChange)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SortChip(ComparisonSort.ALERTS, R.string.compare_sort_alerts, uiState.sort, viewModel::onSortChange)
                                SortChip(ComparisonSort.CRITICAL_ALERTS, R.string.compare_sort_critical, uiState.sort, viewModel::onSortChange)
                                SortChip(ComparisonSort.STOPS, R.string.compare_sort_stops, uiState.sort, viewModel::onSortChange)
                            }
                        }
                    }
                }
                item { ComparisonTable(rows) }
            }
        }
    }
}

@Composable
private fun AssetCheckRow(asset: Asset, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        Column(modifier = Modifier.weight(1f)) {
            Text(text = asset.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                text = asset.code.value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SortChip(
    sort: ComparisonSort,
    label: Int,
    selected: ComparisonSort,
    onSelect: (ComparisonSort) -> Unit
) {
    FilterChip(
        selected = selected == sort,
        onClick = { onSelect(sort) },
        label = { Text(text = stringResource(label)) }
    )
}

@Composable
private fun ComparisonTable(rows: List<AssetComparisonRow>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            TableRow {
                HeaderCell(stringResource(R.string.compare_col_asset), 2.2f, TextAlign.Start)
                HeaderCell(stringResource(R.string.compare_col_availability), 1.6f)
                HeaderCell(stringResource(R.string.compare_col_downtime), 1.5f)
                HeaderCell(stringResource(R.string.compare_col_alerts), 1f)
                HeaderCell(stringResource(R.string.compare_col_critical), 1f)
                HeaderCell(stringResource(R.string.compare_col_stops), 1f)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.primary, thickness = 2.dp)
            rows.forEachIndexed { index, row ->
                if (index > 0) HorizontalDivider()
                val tone = availabilityTone(row.availabilityPercent)
                TableRow {
                    Column(modifier = Modifier.weight(2.2f)) {
                        Text(text = row.assetCode, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold)
                        Text(
                            text = row.assetName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    Box(modifier = Modifier.weight(1.6f), contentAlignment = Alignment.Center) {
                        ColoredStatusChip(text = formatPercent(row.availabilityPercent), tone = tone)
                    }
                    NumberCell(formatDuration(row.downtimeSeconds), 1.5f)
                    NumberCell(row.alerts.toString(), 1f)
                    NumberCell(row.criticalAlerts.toString(), 1f)
                    NumberCell(row.stops.toString(), 1f)
                }
            }
        }
    }
}

@Composable
private fun TableRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        content = content
    )
}

@Composable
private fun RowScope.HeaderCell(text: String, weight: Float, align: TextAlign = TextAlign.Center) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        textAlign = align,
        modifier = Modifier.weight(weight)
    )
}

@Composable
private fun RowScope.NumberCell(text: String, weight: Float) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(weight)
    )
}
