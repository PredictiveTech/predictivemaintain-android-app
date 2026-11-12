package pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.predictivemaintain.app.R
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.NewAsset
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.Criticality
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.AssetFormField
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.CreateAssetError
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.FieldError
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel.CreateAssetViewModel

/** Common asset types offered as suggestions. The backend accepts any text up to 80 characters. */
private val AssetTypeSuggestions = listOf("PUMP", "MOTOR", "COMPRESSOR", "FAN", "GEARBOX")

private val CriticalityOptions = listOf(Criticality.LOW, Criticality.MEDIUM, Criticality.HIGH, Criticality.CRITICAL)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAssetScreen(
    viewModel: CreateAssetViewModel,
    onBackClick: () -> Unit,
    onCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.created) {
        if (state.created) onCreated()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.create_asset_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text(text = "←", style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val enabled = !state.isSubmitting

            FormTextField(
                value = state.code,
                onValueChange = { viewModel.onFieldChange(AssetFormField.CODE, it) },
                label = stringResource(R.string.create_asset_code),
                error = state.fieldErrors[AssetFormField.CODE],
                maxLength = NewAsset.CODE_MAX,
                enabled = enabled
            )
            FormTextField(
                value = state.name,
                onValueChange = { viewModel.onFieldChange(AssetFormField.NAME, it) },
                label = stringResource(R.string.create_asset_name),
                error = state.fieldErrors[AssetFormField.NAME],
                maxLength = NewAsset.NAME_MAX,
                enabled = enabled
            )
            FormTextField(
                value = state.location,
                onValueChange = { viewModel.onFieldChange(AssetFormField.LOCATION, it) },
                label = stringResource(R.string.create_asset_location),
                error = state.fieldErrors[AssetFormField.LOCATION],
                maxLength = NewAsset.LOCATION_MAX,
                enabled = enabled
            )

            AssetTypeField(
                value = state.assetType,
                onValueChange = { viewModel.onFieldChange(AssetFormField.ASSET_TYPE, it) },
                error = state.fieldErrors[AssetFormField.ASSET_TYPE],
                enabled = enabled
            )
            CriticalityField(
                selected = state.criticality,
                onSelected = viewModel::onCriticalityChange,
                enabled = enabled
            )

            FormTextField(
                value = state.productionLine,
                onValueChange = { viewModel.onFieldChange(AssetFormField.PRODUCTION_LINE, it) },
                label = stringResource(R.string.create_asset_production_line),
                error = state.fieldErrors[AssetFormField.PRODUCTION_LINE],
                maxLength = NewAsset.PRODUCTION_LINE_MAX,
                enabled = enabled
            )

            Text(
                text = stringResource(R.string.create_asset_coordinates_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FormTextField(
                    value = state.latitude,
                    onValueChange = { viewModel.onFieldChange(AssetFormField.LATITUDE, it) },
                    label = stringResource(R.string.create_asset_latitude),
                    error = state.fieldErrors[AssetFormField.LATITUDE],
                    enabled = enabled,
                    keyboardType = KeyboardType.Text,
                    modifier = Modifier.weight(1f)
                )
                FormTextField(
                    value = state.longitude,
                    onValueChange = { viewModel.onFieldChange(AssetFormField.LONGITUDE, it) },
                    label = stringResource(R.string.create_asset_longitude),
                    error = state.fieldErrors[AssetFormField.LONGITUDE],
                    enabled = enabled,
                    keyboardType = KeyboardType.Text,
                    modifier = Modifier.weight(1f)
                )
            }

            state.submitError?.let { error ->
                Text(
                    text = submitErrorText(error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = viewModel::submit,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(text = stringResource(R.string.create_asset_submit))
                }
            }
        }
    }
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: FieldError?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    maxLength: Int? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        singleLine = true,
        enabled = enabled,
        isError = error != null,
        supportingText = error?.let { { Text(text = fieldErrorText(it, maxLength)) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssetTypeField(
    value: String,
    onValueChange: (String) -> Unit,
    error: FieldError?,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it && enabled }) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(text = stringResource(R.string.create_asset_type)) },
            singleLine = true,
            enabled = enabled,
            isError = error != null,
            supportingText = error?.let { { Text(text = fieldErrorText(it, NewAsset.ASSET_TYPE_MAX)) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AssetTypeSuggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(text = suggestion) },
                    onClick = {
                        onValueChange(suggestion)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CriticalityField(
    selected: Criticality,
    onSelected: (Criticality) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it && enabled }) {
        OutlinedTextField(
            value = stringResource(criticalityLabel(selected)),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(text = stringResource(R.string.create_asset_criticality)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CriticalityOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = stringResource(criticalityLabel(option))) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun criticalityLabel(criticality: Criticality): Int = when (criticality) {
    Criticality.LOW -> R.string.criticality_low
    Criticality.MEDIUM -> R.string.criticality_medium
    Criticality.HIGH -> R.string.criticality_high
    Criticality.CRITICAL -> R.string.criticality_critical
    Criticality.UNKNOWN -> R.string.status_unknown
}

@Composable
private fun fieldErrorText(error: FieldError, maxLength: Int?): String = when (error) {
    FieldError.REQUIRED -> stringResource(R.string.field_error_required)
    FieldError.TOO_LONG -> stringResource(R.string.field_error_too_long, maxLength ?: 0)
    FieldError.INVALID_NUMBER -> stringResource(R.string.field_error_invalid_number)
    FieldError.OUT_OF_RANGE -> stringResource(R.string.field_error_out_of_range)
    FieldError.COORDINATES_INCOMPLETE -> stringResource(R.string.field_error_coordinates_incomplete)
    FieldError.DUPLICATE_CODE -> stringResource(R.string.create_asset_error_duplicate_short)
}

@Composable
private fun submitErrorText(error: CreateAssetError): String = when (error) {
    CreateAssetError.DuplicateCode -> stringResource(R.string.create_asset_error_duplicate)
    CreateAssetError.CapacityExceeded -> stringResource(R.string.create_asset_error_capacity)
    CreateAssetError.Forbidden -> stringResource(R.string.create_asset_error_forbidden)
    is CreateAssetError.Other -> when (val e = error.error) {
        is AppError.Api -> e.detail
        is AppError.Network -> stringResource(R.string.error_network)
        is AppError.InvalidResponse -> stringResource(R.string.error_invalid_response)
        is AppError.SessionExpired -> stringResource(R.string.error_session_expired)
        is AppError.Unknown -> stringResource(R.string.error_unknown)
    }
}
