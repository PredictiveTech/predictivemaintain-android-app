package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class WorkOrderPageDto(
    val items: List<WorkOrderDto>,
    val totalElements: Long,
    val page: Int,
    val size: Int,
    val totalPages: Int
)
