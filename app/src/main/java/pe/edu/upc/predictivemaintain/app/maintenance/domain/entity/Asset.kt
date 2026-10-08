package pe.edu.upc.predictivemaintain.app.maintenance.domain.entity

import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetCode
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetStatus
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.Criticality

data class Asset(
    val id: AssetId,
    val code: AssetCode,
    val name: String,
    val location: String?,
    val productionLine: String?,
    val assetType: String,
    val criticality: Criticality,
    val active: Boolean,
    val status: AssetStatus,
    val sensorTypes: List<String>
)
