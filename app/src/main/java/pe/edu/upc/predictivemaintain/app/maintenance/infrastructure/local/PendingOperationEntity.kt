package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.PendingOperation
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.SyncOperationType
import java.time.Instant

/** Row of the offline queue (US-28). The primary key is the idempotency key sent to the server. */
@Entity(tableName = "pending_operations")
data class PendingOperationEntity(
    @PrimaryKey
    @ColumnInfo(name = "operation_id") val operationId: String,
    @ColumnInfo(name = "work_order_id") val workOrderId: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "performed_at") val performedAt: String,
    @ColumnInfo(name = "summary") val summary: String?
)

internal fun PendingOperation.toEntity() = PendingOperationEntity(
    operationId = operationId,
    workOrderId = workOrderId,
    userId = userId,
    type = type.name,
    performedAt = performedAt.toString(),
    summary = summary
)

/** Rows that cannot be parsed are dropped, like the DataStore implementation did. */
internal fun PendingOperationEntity.toDomainOrNull(): PendingOperation? = try {
    PendingOperation(
        operationId = operationId,
        workOrderId = workOrderId,
        userId = userId,
        type = SyncOperationType.valueOf(type),
        performedAt = Instant.parse(performedAt),
        summary = summary
    )
} catch (_: Exception) {
    null
}
