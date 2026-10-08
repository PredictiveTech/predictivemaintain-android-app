package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class ReviewAlertRequestDto(
    val status: String,
    val reason: String? = null,
    val expectedVersion: Long
)
