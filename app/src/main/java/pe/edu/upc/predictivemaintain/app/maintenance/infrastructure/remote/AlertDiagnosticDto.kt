package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class AlertDiagnosticDto(
    val metric: String,
    val unit: String,
    val observedValue: Double,
    val lowerBound: Double,
    val upperBound: Double,
    val measuredAt: String
)
