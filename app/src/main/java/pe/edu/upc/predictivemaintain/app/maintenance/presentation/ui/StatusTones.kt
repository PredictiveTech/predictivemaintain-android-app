package pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui

import pe.edu.upc.predictivemaintain.app.core.ui.StatusTone
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertSeverity
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetStatus
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderStatus

internal fun assetStatusTone(status: AssetStatus): StatusTone = when (status) {
    AssetStatus.IN_ALERT -> StatusTone.DANGER
    AssetStatus.OPERATIONAL -> StatusTone.SUCCESS
    AssetStatus.NO_COMMUNICATION -> StatusTone.INFO
    AssetStatus.INACTIVE, AssetStatus.UNKNOWN -> StatusTone.NEUTRAL
}

internal fun severityTone(severity: AlertSeverity): StatusTone = when (severity) {
    AlertSeverity.CRITICAL -> StatusTone.DANGER
    AlertSeverity.WARNING -> StatusTone.WARNING
    AlertSeverity.UNKNOWN -> StatusTone.NEUTRAL
}

internal fun workOrderStatusTone(status: WorkOrderStatus): StatusTone = when (status) {
    WorkOrderStatus.OPEN, WorkOrderStatus.ASSIGNED -> StatusTone.INFO
    WorkOrderStatus.IN_PROGRESS -> StatusTone.WARNING
    WorkOrderStatus.COMPLETED -> StatusTone.SUCCESS
    WorkOrderStatus.CANCELLED, WorkOrderStatus.UNKNOWN -> StatusTone.NEUTRAL
}
