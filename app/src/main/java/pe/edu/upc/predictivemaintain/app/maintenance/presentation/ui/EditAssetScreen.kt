package pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.predictivemaintain.app.R
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel.EditAssetViewModel

@Composable
fun EditAssetScreen(
    viewModel: EditAssetViewModel,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AssetFormScreen(
        title = stringResource(R.string.edit_asset_title),
        submitLabel = stringResource(R.string.edit_asset_submit),
        state = state,
        codeEditable = false,
        onFieldChange = viewModel::onFieldChange,
        onCriticalityChange = viewModel::onCriticalityChange,
        onSubmit = viewModel::submit,
        onRetryLoad = viewModel::load,
        onBackClick = onBackClick,
        onSaved = onSaved,
        modifier = modifier
    )
}
