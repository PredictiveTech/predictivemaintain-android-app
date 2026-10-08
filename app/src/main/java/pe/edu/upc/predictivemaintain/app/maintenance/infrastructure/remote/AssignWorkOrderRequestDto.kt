package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class AssignWorkOrderRequestDto(
    val technicianId: String,
    val expectedVersion: Long
)
