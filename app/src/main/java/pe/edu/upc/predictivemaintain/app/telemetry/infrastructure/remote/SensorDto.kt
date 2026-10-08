package pe.edu.upc.predictivemaintain.app.telemetry.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class SensorItemDto(
    val id: String,
    val metric: String,
    val unit: String,
    val communication: String? = null,
    val latestReading: SensorReadingDto? = null,
    val threshold: ThresholdDto? = null,
    val rangeStatus: String? = null
)

@Serializable
data class SensorReadingDto(
    val value: Double,
    val measuredAt: String
)

@Serializable
data class ThresholdDto(
    val lowerBound: Double,
    val upperBound: Double
)
