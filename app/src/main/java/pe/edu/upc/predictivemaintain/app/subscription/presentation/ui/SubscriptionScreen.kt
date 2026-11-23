package pe.edu.upc.predictivemaintain.app.subscription.presentation.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.predictivemaintain.app.R
import pe.edu.upc.predictivemaintain.app.core.ui.AccentCard
import pe.edu.upc.predictivemaintain.app.core.ui.ColoredStatusChip
import pe.edu.upc.predictivemaintain.app.core.ui.LoadingComponent
import pe.edu.upc.predictivemaintain.app.core.ui.StatusTone
import pe.edu.upc.predictivemaintain.app.core.ui.ToneBanner
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.Subscription
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.SubscriptionStatus
import pe.edu.upc.predictivemaintain.app.subscription.presentation.viewmodel.SubscriptionViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

/** US-18 (usage), US-19 (expiry banner and push destination). Manager only. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel,
    onBackClick: () -> Unit,
    onChangePlanClick: () -> Unit,
    /** True once a plan change made the data on screen stale. */
    needsRefresh: Boolean = false,
    onRefreshed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(needsRefresh) {
        if (needsRefresh) {
            viewModel.load()
            onRefreshed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.subscription_title)) },
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
        if (uiState.isLoading && subscription == null) {
            LoadingComponent(modifier = Modifier.padding(innerPadding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.errorMessage?.let { message ->
                    Text(text = message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = viewModel::load) { Text(text = stringResource(R.string.retry)) }
                }
                if (subscription != null) SubscriptionContent(subscription, onChangePlanClick)
            }
        }
    }
}

@Composable
private fun SubscriptionContent(subscription: Subscription, onChangePlanClick: () -> Unit) {
    val endDate = dateFormatter.format(subscription.endsAt.atZone(ZoneId.systemDefault()))
    val level = usageLevel(subscription)

    // Most urgent first.
    if (subscription.expiry.expired || !subscription.monitoringAllowed) {
        ToneBanner(stringResource(R.string.subscription_expired_banner), StatusTone.DANGER, Icons.Filled.ErrorOutline)
    } else if (subscription.expiry.expiresSoon) {
        val days = subscription.expiry.daysRemaining.toInt()
        ToneBanner(
            text = pluralStringResource(R.plurals.subscription_expires_soon_banner, days, days, endDate),
            tone = StatusTone.WARNING,
            icon = Icons.Filled.Warning
        )
    }
    when (level) {
        UsageLevel.FULL -> ToneBanner(stringResource(R.string.subscription_full_banner), StatusTone.DANGER, Icons.Filled.ErrorOutline)
        UsageLevel.NEAR_LIMIT -> ToneBanner(stringResource(R.string.subscription_near_limit_banner), StatusTone.WARNING, Icons.Filled.Warning)
        UsageLevel.NORMAL -> Unit
    }

    val statusTone = statusTone(subscription.status)
    AccentCard(
        accent = statusTone.accent,
        icon = Icons.Filled.CreditCard,
        title = subscription.plan.name,
        subtitle = stringResource(R.string.subscription_current_plan),
        onClick = null,
        trailing = {
            ColoredStatusChip(
                text = stringResource(
                    when (subscription.status) {
                        SubscriptionStatus.ACTIVE -> R.string.subscription_status_active
                        SubscriptionStatus.SUSPENDED -> R.string.subscription_status_suspended
                        SubscriptionStatus.CANCELLED -> R.string.subscription_status_cancelled
                        SubscriptionStatus.UNKNOWN -> R.string.subscription_status_unknown
                    }
                ),
                tone = statusTone
            )
        }
    ) {
        Text(
            text = formatMoney(subscription.plan.amount, subscription.plan.currency),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    UsageCard(subscription, level)

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.EventRepeat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.subscription_renewal_date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text = endDate, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            if (!subscription.expiry.expired) {
                val days = subscription.expiry.daysRemaining.toInt()
                Text(
                    text = pluralStringResource(R.plurals.subscription_days_remaining, days, days),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))
    Button(onClick = onChangePlanClick, modifier = Modifier.fillMaxWidth()) {
        Text(text = stringResource(R.string.subscription_change_plan))
    }
}

@Composable
private fun UsageCard(subscription: Subscription, level: UsageLevel) {
    val capacity = subscription.capacity
    val tone = usageTone(level)
    val barColor = tone?.accent ?: MaterialTheme.colorScheme.primary

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = stringResource(R.string.subscription_assets_in_use),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = stringResource(R.string.subscription_assets_of, capacity.used, capacity.limit),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            LinearProgressIndicator(
                progress = { usageFraction(capacity.usagePercent) },
                color = barColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = stringResource(R.string.subscription_usage_percent, formatUsagePercent(capacity.usagePercent)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.subscription_available_assets, capacity.available),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
