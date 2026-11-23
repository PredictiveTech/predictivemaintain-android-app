package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote

import kotlinx.serialization.Serializable

// Shapes of GET /reports/availability and /reports/comparison, copied from the OpenAPI spec.
// The spec marks no field as required, so everything the screen depends on is checked by the mapper.

@Serializable
data class AssetEventDto(
    val type: String? = null,
    val referenceId: String? = null,
    val occurredAt: String? = null,
    val endedAt: String? = null,
    val severity: String? = null,
    val status: String? = null
)

@Serializable
data class AvailabilityReportDto(
    val assetId: String? = null,
    val assetCode: String? = null,
    val from: String? = null,
    val to: String? = null,
    val availabilityPercent: Double? = null,
    val downtimeSeconds: Long? = null,
    val periodSeconds: Long? = null,
    val hasEvents: Boolean = false,
    val events: List<AssetEventDto> = emptyList()
)

@Serializable
data class ComparisonRowDto(
    val assetId: String? = null,
    val assetCode: String? = null,
    val assetName: String? = null,
    val alerts: Int? = null,
    val criticalAlerts: Int? = null,
    val stops: Int? = null,
    val downtimeSeconds: Long? = null,
    val availabilityPercent: Double? = null
)
