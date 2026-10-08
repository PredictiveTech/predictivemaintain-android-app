package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class WorkOrderDto(
    val id: String,
    val alertId: String,
    val assetId: String,
    val assetCode: String,
    val assetName: String,
    val severity: String,
    val assignedUserId: String? = null,
    val status: String,
    val summary: String? = null,
    val openedAt: String,
    val completedAt: String? = null,
    val version: Long
)
