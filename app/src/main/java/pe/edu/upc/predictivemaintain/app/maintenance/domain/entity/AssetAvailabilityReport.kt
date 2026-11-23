package pe.edu.upc.predictivemaintain.app.maintenance.domain.entity

import java.time.Instant

enum class AssetEventType { ALERT, DOWNTIME, UNKNOWN }

/** An alert or a stop of an asset. A stop that has not ended yet has no [endedAt]. */
data class AssetEvent(
    val type: AssetEventType,
    val referenceId: String,
    val occurredAt: Instant,
    val endedAt: Instant?,
    val severity: String?,
    val status: String?
)

data class AssetAvailabilityReport(
    val assetId: String,
    val assetCode: String,
    val from: Instant,
    val to: Instant,
    val availabilityPercent: Double,
    val downtimeSeconds: Long,
    val periodSeconds: Long,
    val events: List<AssetEvent>
) {
    val downtimeIntervals: List<AssetEvent> get() = events.filter { it.type == AssetEventType.DOWNTIME }
    val alerts: List<AssetEvent> get() = events.filter { it.type == AssetEventType.ALERT }
}
