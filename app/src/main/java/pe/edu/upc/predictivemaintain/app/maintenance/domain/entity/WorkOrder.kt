package pe.edu.upc.predictivemaintain.app.maintenance.domain.entity

import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertSeverity
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.TechnicianId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderStatus
import java.time.Instant

data class WorkOrder(
    val id: WorkOrderId,
    val alertId: AlertId,
    val assetId: AssetId,
    val assetCode: String,
    val assetName: String,
    val severity: AlertSeverity,
    val assignedUserId: TechnicianId?,
    val status: WorkOrderStatus,
    val summary: String?,
    val openedAt: Instant,
    val completedAt: Instant?,
    val version: Long
)
