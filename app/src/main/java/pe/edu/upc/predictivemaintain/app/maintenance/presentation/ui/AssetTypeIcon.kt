package pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Icon for an asset type, using only material-icons-core (no extended set). The closest available
 * glyphs are used: rotation arrows for rotating machinery, a gear/wrench for process equipment.
 * Matching is by keyword so variants such as "CENTRIFUGAL_PUMP" still resolve.
 */
internal fun assetTypeIcon(assetType: String): ImageVector {
    val type = assetType.trim().uppercase()
    return when {
        "COMPRESS" in type -> Icons.Filled.Settings
        "EXTRUDER" in type -> Icons.Filled.Build
        "MOTOR" in type || "ROTATING" in type -> Icons.Filled.Refresh
        "PUMP" in type || "BOMBA" in type -> Icons.Filled.Refresh
        "CONVEYOR" in type || "FAJA" in type -> Icons.Filled.ArrowForward
        else -> Icons.Filled.Info
    }
}
