package pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject

import java.time.Instant

data class AlertDiagnostic(
    val metric: String,
    val unit: String,
    val observedValue: Double,
    val lowerBound: Double,
    val upperBound: Double,
    val measuredAt: Instant
)
