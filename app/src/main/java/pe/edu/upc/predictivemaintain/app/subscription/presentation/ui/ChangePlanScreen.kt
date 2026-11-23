package pe.edu.upc.predictivemaintain.app.subscription.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.predictivemaintain.app.R
import pe.edu.upc.predictivemaintain.app.core.ui.AccentCard
import pe.edu.upc.predictivemaintain.app.core.ui.ColoredStatusChip
import pe.edu.upc.predictivemaintain.app.core.ui.LoadingComponent
import pe.edu.upc.predictivemaintain.app.core.ui.StatusTone
import pe.edu.upc.predictivemaintain.app.core.ui.ToneBanner
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.SubscriptionPlan
import pe.edu.upc.predictivemaintain.app.subscription.presentation.state.PlanChangeFailure
import pe.edu.upc.predictivemaintain.app.subscription.presentation.viewmodel.ChangePlanViewModel

/** US-17: available plans (the current one marked) and the plan change. Manager only. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePlanScreen(
    viewModel: ChangePlanViewModel,
    onBackClick: () -> Unit,
    onChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.changed) {
        if (uiState.changed) onChanged()
    }

    uiState.planToConfirm?.let { plan ->
        AlertDialog(
            onDismissRequest = viewModel::dismissConfirmation,
            title = { Text(text = stringResource(R.string.plans_confirm_title)) },
            text = {
                Text(
                    text = stringResource(
                        R.string.plans_confirm_message,
                        plan.name,
                        plan.assetLimit,
                        formatMoney(plan.amount, plan.currency)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmChange) { Text(text = stringResource(R.string.plans_confirm_action)) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissConfirmation) { Text(text = stringResource(R.string.cancel)) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.plans_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.subscription_back)
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        val subscription = uiState.subscription
        when {
            uiState.isLoading && subscription == null ->
                LoadingComponent(modifier = Modifier.padding(innerPadding))

            uiState.loadError != null && subscription == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.loadError.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                    TextButton(onClick = viewModel::load) { Text(text = stringResource(R.string.retry)) }
                }
            }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.isChanging) {
                    item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
                }
                uiState.failure?.let { failure ->
                    item {
                        ToneBanner(
                            text = failureText(failure),
                            tone = if (failure is PlanChangeFailure.PlanTooSmall) StatusTone.WARNING else StatusTone.DANGER,
                            icon = Icons.Filled.Warning
                        )
                    }
                }
                if (uiState.plans.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.plans_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(uiState.plans, key = { it.id }) { plan ->
                    val isCurrent = plan.id == uiState.currentPlanId
                    PlanCard(
                        plan = plan,
                        isCurrent = isCurrent,
                        assetsToDeactivate = subscription?.assetsToDeactivateFor(plan) ?: 0,
                        enabled = !uiState.isChanging,
                        onClick = { viewModel.onPlanSelected(plan) }
                    )
                }
            }
        }
    }
}

@Composable
private fun failureText(failure: PlanChangeFailure): String = when (failure) {
    is PlanChangeFailure.PlanTooSmall -> pluralStringResource(
        R.plurals.plans_too_small_error,
        failure.assetsToDeactivate,
        failure.plan.name,
        failure.plan.assetLimit,
        failure.assetsInUse,
        failure.assetsToDeactivate
    )
    is PlanChangeFailure.Other -> failure.message
}

@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    isCurrent: Boolean,
    assetsToDeactivate: Int,
    enabled: Boolean,
    onClick: () -> Unit
) {
    AccentCard(
        accent = if (isCurrent) StatusTone.SUCCESS.accent else StatusTone.INFO.accent,
        icon = Icons.Filled.Inventory2,
        title = plan.name,
        subtitle = stringResource(R.string.plans_up_to_assets, plan.assetLimit),
        // The current plan is not tappable: asking for it again changes nothing.
        onClick = if (isCurrent || !enabled) null else onClick,
        trailing = {
            if (isCurrent) {
                ColoredStatusChip(text = stringResource(R.string.plans_current_chip), tone = StatusTone.SUCCESS)
            }
        }
    ) {
        Text(
            text = formatMoney(plan.amount, plan.currency),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!isCurrent && assetsToDeactivate > 0) {
            Text(
                text = pluralStringResource(R.plurals.plans_deactivate_first, assetsToDeactivate, assetsToDeactivate),
                style = MaterialTheme.typography.bodySmall,
                color = StatusTone.WARNING.content,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
