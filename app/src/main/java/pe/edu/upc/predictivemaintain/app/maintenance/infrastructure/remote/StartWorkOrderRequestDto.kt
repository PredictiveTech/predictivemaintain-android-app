package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class StartWorkOrderRequestDto(
    val expectedVersion: Long
)
