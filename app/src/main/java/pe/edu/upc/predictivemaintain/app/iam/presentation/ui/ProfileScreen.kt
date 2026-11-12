package pe.edu.upc.predictivemaintain.app.iam.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.predictivemaintain.app.BuildConfig
import pe.edu.upc.predictivemaintain.app.R
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role
import pe.edu.upc.predictivemaintain.app.iam.presentation.state.ProfileUiState
import pe.edu.upc.predictivemaintain.app.iam.presentation.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = stringResource(R.string.profile_title)) }) },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                uiState.isLoading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                uiState.loadError != null -> LoadError(
                    error = uiState.loadError!!,
                    onRetry = viewModel::loadProfile
                )

                else -> {
                    ProfileHeader(uiState)
                    EditProfileCard(
                        uiState = uiState,
                        onDisplayNameChange = viewModel::onDisplayNameChange,
                        onSave = viewModel::save
                    )
                }
            }

            AppInfoCard()

            OutlinedButton(
                onClick = { viewModel.logout(onLoggedOut) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.home_logout))
            }
        }
    }
}

@Composable
private fun ProfileHeader(uiState: ProfileUiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(52.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = uiState.savedDisplayName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = uiState.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            uiState.roles.forEach { role -> RoleChip(role) }
        }
        if (uiState.isOffline) {
            Text(
                text = stringResource(R.string.profile_offline),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun RoleChip(role: Role) {
    Text(
        text = stringResource(roleLabel(role)),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
private fun EditProfileCard(
    uiState: ProfileUiState,
    onDisplayNameChange: (String) -> Unit,
    onSave: () -> Unit
) {
    SectionCard(title = stringResource(R.string.profile_edit_title)) {
        OutlinedTextField(
            value = uiState.email,
            onValueChange = {},
            readOnly = true,
            label = { Text(text = stringResource(R.string.profile_email)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.displayName,
            onValueChange = onDisplayNameChange,
            singleLine = true,
            enabled = !uiState.isSaving,
            isError = uiState.isDisplayNameBlank,
            label = { Text(text = stringResource(R.string.profile_display_name)) },
            supportingText = if (uiState.isDisplayNameBlank) {
                { Text(text = stringResource(R.string.profile_display_name_blank)) }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )
        uiState.saveError?.let {
            Text(
                text = errorText(it),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        if (uiState.saveSucceeded) {
            Text(
                text = stringResource(R.string.profile_saved),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Button(
            onClick = onSave,
            enabled = uiState.canSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text(text = stringResource(R.string.profile_save))
            }
        }
    }
}

@Composable
private fun AppInfoCard() {
    SectionCard(title = stringResource(R.string.profile_app_info)) {
        InfoRow(stringResource(R.string.profile_app_name), stringResource(R.string.app_name))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        InfoRow(stringResource(R.string.profile_app_version), BuildConfig.VERSION_NAME)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            content()
        }
    }
}

@Composable
private fun LoadError(error: AppError, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = errorText(error),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Button(onClick = onRetry) { Text(text = stringResource(R.string.retry)) }
    }
}

@Composable
private fun errorText(error: AppError): String = when (error) {
    is AppError.Api -> error.detail
    is AppError.Network -> stringResource(R.string.error_network)
    is AppError.InvalidResponse -> stringResource(R.string.error_invalid_response)
    is AppError.SessionExpired -> stringResource(R.string.error_session_expired)
    is AppError.Unknown -> stringResource(R.string.error_unknown)
}

private fun roleLabel(role: Role): Int = when (role) {
    Role.MAINTENANCE_MANAGER -> R.string.role_maintenance_manager
    Role.TECHNICIAN -> R.string.role_technician
    Role.OPERATOR -> R.string.role_operator
}
