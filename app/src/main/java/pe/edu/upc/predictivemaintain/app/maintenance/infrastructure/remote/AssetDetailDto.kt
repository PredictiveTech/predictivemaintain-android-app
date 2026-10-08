package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class AssetDetailDto(
    val id: String,
    val code: String,
    val name: String,
    val location: String? = null,
    val productionLine: String? = null,
    val assetType: String,
    val criticality: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val active: Boolean
)
