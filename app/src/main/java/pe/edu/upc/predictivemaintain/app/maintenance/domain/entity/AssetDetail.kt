package pe.edu.upc.predictivemaintain.app.maintenance.domain.entity

import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetCode
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.Criticality

data class AssetDetail(
    val id: AssetId,
    val code: AssetCode,
    val name: String,
    val location: String?,
    val productionLine: String?,
    val assetType: String,
    val criticality: Criticality,
    val latitude: Double?,
    val longitude: Double?,
    val active: Boolean
)
