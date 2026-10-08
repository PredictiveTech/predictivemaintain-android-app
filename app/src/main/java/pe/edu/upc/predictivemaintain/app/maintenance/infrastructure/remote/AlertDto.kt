package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class AlertDto(
    val id: String,
    val assetId: String,
    val assetCode: String,
    val assetName: String,
    val severity: String? = null,
    val status: String? = null,
    val raisedAt: String,
    val discardReason: String? = null,
    val version: Long,
    val diagnostic: AlertDiagnosticDto? = null
)
