package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class CreateWorkOrderRequestDto(
    val alertId: String
)
