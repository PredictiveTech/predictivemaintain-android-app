package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.WorkOrder
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertSeverity
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.TechnicianId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderStatus
import java.time.Instant

/** Local copy of a work order, so it can be opened and acted on without connection, even after a restart. */
@Entity(tableName = "work_orders")
data class WorkOrderEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "alert_id") val alertId: String,
    @ColumnInfo(name = "asset_id") val assetId: String,
    @ColumnInfo(name = "asset_code") val assetCode: String,
    @ColumnInfo(name = "asset_name") val assetName: String,
    @ColumnInfo(name = "severity") val severity: String,
    @ColumnInfo(name = "assigned_user_id") val assignedUserId: String?,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "summary") val summary: String?,
    @ColumnInfo(name = "opened_at") val openedAt: String,
    @ColumnInfo(name = "completed_at") val completedAt: String?,
    @ColumnInfo(name = "version") val version: Long,
    /** Epoch millis of the last time this row was written. */
    @ColumnInfo(name = "cached_at") val cachedAt: Long
)

internal fun WorkOrder.toEntity(cachedAt: Long) = WorkOrderEntity(
    id = id.value,
    alertId = alertId.value,
    assetId = assetId.value,
    assetCode = assetCode,
    assetName = assetName,
    severity = severity.name,
    assignedUserId = assignedUserId?.value,
    status = status.name,
    summary = summary,
    openedAt = openedAt.toString(),
    completedAt = completedAt?.toString(),
    version = version,
    cachedAt = cachedAt
)

/** A row that cannot be turned back into a valid order is treated as a cache miss. */
internal fun WorkOrderEntity.toDomainOrNull(): WorkOrder? = try {
    WorkOrder(
        id = WorkOrderId(id),
        alertId = AlertId(alertId),
        assetId = AssetId(assetId),
        assetCode = assetCode,
        assetName = assetName,
        severity = AlertSeverity.fromString(severity),
        assignedUserId = assignedUserId?.let { TechnicianId(it) },
        status = WorkOrderStatus.fromString(status),
        summary = summary,
        openedAt = Instant.parse(openedAt),
        completedAt = completedAt?.let { Instant.parse(it) },
        version = version
    )
} catch (_: Exception) {
    null
}
