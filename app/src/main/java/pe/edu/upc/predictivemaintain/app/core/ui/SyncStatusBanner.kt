package pe.edu.upc.predictivemaintain.app.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pe.edu.upc.predictivemaintain.app.R

/** Shows the offline / syncing / pending state. Draws nothing when there is nothing to tell. */
@Composable
fun SyncStatusBanner(
    isOffline: Boolean,
    isSyncing: Boolean,
    pendingCount: Int,
    modifier: Modifier = Modifier
) {
    val text = when {
        isOffline && pendingCount > 0 -> stringResource(R.string.sync_offline_with_pending, pendingCount)
        isOffline -> stringResource(R.string.sync_offline)
        isSyncing -> stringResource(R.string.sync_syncing, pendingCount)
        pendingCount > 0 -> stringResource(R.string.sync_pending, pendingCount)
        else -> return
    }
    val container = if (isOffline) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
    val content = if (isOffline) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer

    Surface(color = container, contentColor = content, modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
