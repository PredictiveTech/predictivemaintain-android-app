package pe.edu.upc.predictivemaintain.app.maintenance.domain.sync

import java.time.Instant

enum class SyncOperationType { START, COMPLETE }

enum class SyncOutcome {
    APPLIED,
    ALREADY_APPLIED,
    SUPERSEDED,
    REJECTED;

    companion object {
        fun fromString(value: String?): SyncOutcome = when (value?.uppercase()) {
            "APPLIED" -> APPLIED
            "ALREADY_APPLIED" -> ALREADY_APPLIED
            "SUPERSEDED" -> SUPERSEDED
            else -> REJECTED
        }
    }
}

/** An action the technician did without connection and that still has to reach the server. */
data class PendingOperation(
    val operationId: String,
    val workOrderId: String,
    val userId: String,
    val type: SyncOperationType,
    val performedAt: Instant,
    val summary: String?
)

data class SyncItemResult(
    val operationId: String,
    val workOrderId: String,
    val outcome: SyncOutcome,
    val message: String?,
    val workOrderStatus: String?,
    val workOrderVersion: Long?
) {
    /** The server did not do what the technician asked: the technician must be told. */
    val needsAttention: Boolean
        get() = outcome == SyncOutcome.SUPERSEDED || outcome == SyncOutcome.REJECTED
}

data class SyncReport(val items: List<SyncItemResult>) {
    val appliedCount: Int get() = items.count { !it.needsAttention }
    val issues: List<SyncItemResult> get() = items.filter { it.needsAttention }
}
