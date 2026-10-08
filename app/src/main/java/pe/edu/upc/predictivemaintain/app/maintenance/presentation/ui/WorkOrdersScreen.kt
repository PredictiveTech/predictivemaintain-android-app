package pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.predictivemaintain.app.R
import pe.edu.upc.predictivemaintain.app.core.ui.EmptyStateComponent
import pe.edu.upc.predictivemaintain.app.core.ui.ErrorStateComponent
import pe.edu.upc.predictivemaintain.app.core.ui.LoadingComponent
import pe.edu.upc.predictivemaintain.app.core.ui.StatusChip
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.WorkOrder
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertSeverity
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderStatus
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel.WorkOrdersViewModel
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrdersScreen(
    viewModel: WorkOrdersViewModel = hiltViewModel(),
    onOrderClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    val title = if (uiState.isTechnician && !uiState.isManager) {
        stringResource(R.string.orders_my_title)
    } else {
        stringResource(R.string.orders_title)
    }

    val filterList = if (uiState.isTechnician && !uiState.isManager) {
        listOf(
            null to stringResource(R.string.orders_filter_all),
            WorkOrderStatus.ASSIGNED to stringResource(R.string.orders_filter_assigned),
            WorkOrderStatus.IN_PROGRESS to stringResource(R.string.orders_filter_in_progress),
            WorkOrderStatus.COMPLETED to stringResource(R.string.orders_filter_completed)
        )
    } else {
        listOf(
            null to stringResource(R.string.orders_filter_all),
            WorkOrderStatus.OPEN to stringResource(R.string.orders_filter_open),
            WorkOrderStatus.ASSIGNED to stringResource(R.string.orders_filter_assigned),
            WorkOrderStatus.IN_PROGRESS to stringResource(R.string.orders_filter_in_progress),
            WorkOrderStatus.COMPLETED to stringResource(R.string.orders_filter_completed)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title) }
            )
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
                filterList.forEach { (status, label) ->
                    FilterChip(
                        selected = uiState.selectedStatus == status,
                        onClick = { viewModel.filterByStatus(status) },
                        label = { Text(text = label) }
                    )
                }
            }

            when {
                uiState.isLoading && uiState.orders.isEmpty() -> {
                    LoadingComponent()
                }
                uiState.isErrorState -> {
                    ErrorStateComponent(
                        message = uiState.errorMessage ?: stringResource(R.string.error_unknown),
                        onRetry = { viewModel.refresh() }
                    )
                }
                uiState.orders.isEmpty() -> {
                    EmptyStateComponent(message = stringResource(R.string.orders_no_orders))
                }
                else -> {
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = { viewModel.refresh() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.orders,
                                key = { it.id.value }
                            ) { order ->
                                WorkOrderCard(
                                    order = order,
                                    technicianName = order.assignedUserId?.value?.let { uiState.technicianNames[it] },
                                    onClick = { onOrderClick(order.id.value) }
                                )
                            }

                            if (uiState.currentPage + 1 < uiState.totalPages) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (uiState.isLoadingMore) {
                                            LoadingComponent()
                                        } else {
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
    }
}

@Composable
private fun WorkOrderCard(
    order: WorkOrder,
    technicianName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "${order.assetCode} - ${order.assetName}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusChip(text = localizedSeverity(order.severity))
                StatusChip(text = localizedStatus(order.status))
            }

            Spacer(modifier = Modifier.height(8.dp))

            val ageText = orderAgeText(order.openedAt)
            Text(
                text = ageText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val techDisplay = technicianName ?: stringResource(R.string.orders_unassigned)
            Text(
                text = stringResource(R.string.orders_assigned_technician, techDisplay),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun localizedSeverity(severity: AlertSeverity): String {
    return when (severity) {
        AlertSeverity.WARNING -> stringResource(R.string.orders_severity_warning)
        AlertSeverity.CRITICAL -> stringResource(R.string.orders_severity_critical)
        AlertSeverity.UNKNOWN -> stringResource(R.string.orders_severity_unknown)
    }
}

@Composable
internal fun localizedStatus(status: WorkOrderStatus): String {
    return when (status) {
        WorkOrderStatus.OPEN -> stringResource(R.string.orders_status_open)
        WorkOrderStatus.ASSIGNED -> stringResource(R.string.orders_status_assigned)
        WorkOrderStatus.IN_PROGRESS -> stringResource(R.string.orders_status_in_progress)
        WorkOrderStatus.COMPLETED -> stringResource(R.string.orders_status_completed)
        WorkOrderStatus.CANCELLED -> stringResource(R.string.orders_status_cancelled)
        WorkOrderStatus.UNKNOWN -> stringResource(R.string.orders_status_unknown)
    }
}

@Composable
private fun orderAgeText(openedAt: Instant): String {
    return when (val age = alertAgeOf(openedAt, Instant.now())) {
        AlertAge.JustNow -> stringResource(R.string.alerts_age_now)
        is AlertAge.Minutes -> stringResource(R.string.alerts_age_minutes, age.value)
        is AlertAge.Hours -> stringResource(R.string.alerts_age_hours, age.value)
        is AlertAge.Days -> stringResource(R.string.alerts_age_days, age.value)
    }
}
