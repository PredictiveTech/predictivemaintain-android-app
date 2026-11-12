package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class UpdateAssetRequestDto(
    val name: String,
    val location: String,
    val assetType: String,
    val criticality: String,
    val plantId: String? = null,
    val productionLine: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
