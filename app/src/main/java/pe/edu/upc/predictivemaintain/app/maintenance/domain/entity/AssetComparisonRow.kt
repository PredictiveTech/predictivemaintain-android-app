package pe.edu.upc.predictivemaintain.app.maintenance.domain.entity

data class AssetComparisonRow(
    val assetId: String,
    val assetCode: String,
    val assetName: String,
    val alerts: Int,
    val criticalAlerts: Int,
    val stops: Int,
    val downtimeSeconds: Long,
    val availabilityPercent: Double
)
