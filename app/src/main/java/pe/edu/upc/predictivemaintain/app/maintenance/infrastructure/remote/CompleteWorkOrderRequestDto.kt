package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class CompleteWorkOrderRequestDto(
    val status: String,
    val summary: String,
    val expectedVersion: Long
)
