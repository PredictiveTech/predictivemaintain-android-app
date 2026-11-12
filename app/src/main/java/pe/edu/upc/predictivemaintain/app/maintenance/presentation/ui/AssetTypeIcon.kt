package pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Icon for an asset type (material-icons-extended). Matching is by keyword so variants such as
 * "CENTRIFUGAL_PUMP" still resolve; anything unknown gets the generic icon.
 * There is no conveyor-belt glyph in the extended set shipped with this Compose version, so a stacked-boxes
 * icon is used for conveyors.
 */
internal fun assetTypeIcon(assetType: String): ImageVector {
    val type = assetType.trim().uppercase()
    return when {
        "COMPRES" in type -> Icons.Filled.Air
        "EXTRUDER" in type -> Icons.Filled.PrecisionManufacturing
        "MOTOR" in type || "ROTATING" in type -> Icons.Filled.Bolt
        "PUMP" in type || "BOMBA" in type -> Icons.Filled.WaterDrop
        "CONVEYOR" in type || "FAJA" in type -> Icons.Filled.Inventory2
        else -> Icons.Filled.Info
    }
}
