package pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject

import java.time.Instant

data class SensorReading(
    val value: Double,
    val measuredAt: Instant
)
