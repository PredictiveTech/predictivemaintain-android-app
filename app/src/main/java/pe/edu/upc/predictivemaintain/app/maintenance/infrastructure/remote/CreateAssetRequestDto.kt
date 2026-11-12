package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class CreateAssetRequestDto(
    val code: String,
    val name: String,
    val location: String,
    val assetType: String,
    val criticality: String,
    val productionLine: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
