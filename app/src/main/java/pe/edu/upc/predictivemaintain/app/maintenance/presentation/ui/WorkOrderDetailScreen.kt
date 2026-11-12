package pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import pe.edu.upc.predictivemaintain.app.BuildConfig
import pe.edu.upc.predictivemaintain.app.R
import pe.edu.upc.predictivemaintain.app.core.ui.LoadingComponent
import pe.edu.upc.predictivemaintain.app.core.ui.StatusChip
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Evidence
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel.WorkOrderDetailViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderDetailScreen(
    orderId: String,
    viewModel: WorkOrderDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAssignDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }
    var completeSummaryText by remember { mutableStateOf("") }

    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.orders_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text(text = "←", style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (uiState.isLoading && uiState.order == null) {
            LoadingComponent(modifier = Modifier.padding(innerPadding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                uiState.errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                uiState.successMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (uiState.isPendingSync) {
                    Text(
                        text = stringResource(R.string.orders_pending_sync),
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                uiState.order?.let { order ->
                    Text(
                        text = stringResource(R.string.orders_detail_asset, order.assetCode, order.assetName),
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusChip(text = localizedSeverity(order.severity))
                        StatusChip(text = localizedStatus(order.status))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val techName = uiState.assignedTechnicianName ?: stringResource(R.string.orders_unassigned)
                    Text(
                        text = stringResource(R.string.orders_assigned_technician, techName),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.orders_detail_opened, order.openedAt.toString()),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (order.completedAt != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.orders_detail_completed, order.completedAt.toString()),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (!order.summary.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.orders_detail_summary, order.summary),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    EvidenceSection(
                        attachments = uiState.attachments,
                        loadFailed = uiState.attachmentsLoadFailed
                    )

                    if (uiState.canAssign) {
                        Spacer(modifier = Modifier.height(24.dp))
                        val buttonText = if (order.assignedUserId == null) {
                            stringResource(R.string.orders_assign_button)
                        } else {
                            stringResource(R.string.orders_reassign_button)
                        }
                        Button(
                            onClick = { showAssignDialog = true },
                            enabled = !uiState.isSubmitting,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = buttonText)
                        }
                    }

                    if (uiState.canStart) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.startWorkOrder() },
                            enabled = !uiState.isSubmitting,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = stringResource(R.string.orders_start_button))
                        }
                    }

                    if (uiState.canComplete) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                completeSummaryText = ""
                                viewModel.setPhotoUri(null)
                                showCompleteDialog = true
                            },
                            enabled = !uiState.isSubmitting,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = stringResource(R.string.orders_complete_button))
                        }
                    }
                }
            }
        }

        if (showAssignDialog) {
            AlertDialog(
                onDismissRequest = { showAssignDialog = false },
                title = { Text(text = stringResource(R.string.orders_dialog_select_technician)) },
                text = {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.availableTechnicians) { tech ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showAssignDialog = false
                                        viewModel.assignTechnician(tech.id.value)
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = tech.displayName.value,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = tech.email.value,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    Button(onClick = { showAssignDialog = false }) {
                        Text(text = stringResource(R.string.orders_cancel))
                    }
                }
            )
        }

        if (showCompleteDialog) {
            val context = LocalContext.current
            var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
            var showPhotoSourceDialog by remember { mutableStateOf(false) }
            var cameraPermissionDenied by remember { mutableStateOf(false) }

            val cameraLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.TakePicture()
            ) { success ->
                if (success && tempCameraUri != null) {
                    viewModel.setPhotoUri(tempCameraUri.toString())
                }
            }

            val cameraPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                cameraPermissionDenied = !isGranted
                if (isGranted) {
                    try {
                        val photoFile = File(context.cacheDir, "work_order_${System.currentTimeMillis()}.jpg")
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            photoFile
                        )
                        tempCameraUri = uri
                        cameraLauncher.launch(uri)
                    } catch (_: Exception) {}
                }
            }

            val galleryLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri ->
                if (uri != null) {
                    viewModel.setPhotoUri(uri.toString())
                }
            }

            AlertDialog(
                onDismissRequest = {
                    showCompleteDialog = false
                    viewModel.setPhotoUri(null)
                },
                title = { Text(text = stringResource(R.string.orders_complete_dialog_title)) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = completeSummaryText,
                            onValueChange = { completeSummaryText = it },
                            label = { Text(text = stringResource(R.string.orders_complete_summary_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 6
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${completeSummaryText.length}/2000",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (completeSummaryText.length > 2000) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.End)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { showPhotoSourceDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = stringResource(R.string.orders_attach_photo))
                        }

                        if (cameraPermissionDenied) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.orders_camera_permission_required),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        if (!uiState.selectedPhotoUri.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.orders_photo_preview),
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            AsyncImage(
                                model = uiState.selectedPhotoUri,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val text = completeSummaryText
                            showCompleteDialog = false
                            viewModel.completeWorkOrder(text)
                        },
                        enabled = completeSummaryText.isNotBlank() && completeSummaryText.length <= 2000
                    ) {
                        Text(text = stringResource(R.string.orders_submit))
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        showCompleteDialog = false
                        viewModel.setPhotoUri(null)
                    }) {
                        Text(text = stringResource(R.string.orders_cancel))
                    }
                }
            )

            if (showPhotoSourceDialog) {
                AlertDialog(
                    onDismissRequest = { showPhotoSourceDialog = false },
                    title = { Text(text = stringResource(R.string.orders_photo_source_title)) },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    showPhotoSourceDialog = false
                                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = stringResource(R.string.orders_photo_camera))
                            }
                            Button(
                                onClick = {
                                    showPhotoSourceDialog = false
                                    galleryLauncher.launch("image/*")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = stringResource(R.string.orders_photo_gallery))
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        Button(onClick = { showPhotoSourceDialog = false }) {
                            Text(text = stringResource(R.string.orders_cancel))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun EvidenceSection(
    attachments: List<Evidence>,
    loadFailed: Boolean
) {
    Text(
        text = stringResource(R.string.orders_evidence_title),
        style = MaterialTheme.typography.titleMedium
    )
    Spacer(modifier = Modifier.height(8.dp))
    when {
        loadFailed -> Text(
            text = stringResource(R.string.orders_evidence_load_failed),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        attachments.isEmpty() -> Text(
            text = stringResource(R.string.orders_evidence_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // The images are protected: the app-wide Coil ImageLoader (see PredictiveMaintainApplication)
            // sends the Bearer token through the OkHttp client's AuthInterceptor.
            attachments.forEach { evidence ->
                AsyncImage(
                    model = evidenceImageUrl(evidence),
                    contentDescription = stringResource(R.string.orders_evidence_title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

private fun evidenceImageUrl(evidence: Evidence): String =
    "${BuildConfig.BASE_URL}work-orders/${evidence.workOrderId.value}/attachments/${evidence.id.value}"
