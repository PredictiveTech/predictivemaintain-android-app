package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class EvidenceDto(
    val id: String,
    val workOrderId: String,
    val mimeType: String,
    val uploadedAt: String
)
