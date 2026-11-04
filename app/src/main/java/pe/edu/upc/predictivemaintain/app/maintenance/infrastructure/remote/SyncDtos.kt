package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class SyncOperationDto(
    val operationId: String,
    val workOrderId: String,
    val type: String,
    val performedAt: String,
    val summary: String? = null
)

@Serializable
data class SyncRequestDto(
    val deviceSentAt: String,
    val operations: List<SyncOperationDto>
)

@Serializable
data class SyncResultDto(
    val operationId: String,
    val outcome: String,
    val message: String? = null,
    val workOrderStatus: String? = null,
    val workOrderVersion: Long? = null
)

@Serializable
data class SyncResponseDto(
    val serverTime: String? = null,
    val clockOffsetSeconds: Long = 0,
    val results: List<SyncResultDto> = emptyList()
)
