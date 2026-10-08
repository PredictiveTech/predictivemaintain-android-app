package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.mapper

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.paging.PageResult
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.WorkOrder
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertSeverity
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.TechnicianId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderStatus
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.WorkOrderDto
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.WorkOrderPageDto
import java.time.Instant
import java.time.format.DateTimeParseException

object WorkOrderMapper {

    fun toDomain(dto: WorkOrderDto): Outcome<WorkOrder> = mapping {
        WorkOrder(
            id = field("id") { WorkOrderId(dto.id) },
            alertId = field("alertId") { AlertId(dto.alertId) },
            assetId = field("assetId") {
                require(dto.assetId.isNotBlank()) { "AssetId cannot be blank" }
                AssetId(dto.assetId)
            },
            assetCode = field("assetCode") {
                require(dto.assetCode.isNotBlank()) { "AssetCode cannot be blank" }
                dto.assetCode
            },
            assetName = field("assetName") {
                require(dto.assetName.isNotBlank()) { "AssetName cannot be blank" }
                dto.assetName
            },
            severity = AlertSeverity.fromString(dto.severity),
            assignedUserId = dto.assignedUserId?.takeIf { it.isNotBlank() }?.let { field("assignedUserId") { TechnicianId(it) } },
            status = WorkOrderStatus.fromString(dto.status),
            summary = dto.summary,
            openedAt = field("openedAt") { Instant.parse(dto.openedAt) },
            completedAt = dto.completedAt?.let { field("completedAt") { Instant.parse(it) } },
            version = field("version") {
                require(dto.version >= 0) { "Version must be non-negative" }
                dto.version
            }
        )
    }

    fun toDomain(dto: WorkOrderPageDto): Outcome<PageResult<WorkOrder>> = mapping {
        val mappedItems = dto.items.map { itemDto ->
            when (val result = toDomain(itemDto)) {
                is Outcome.Success -> result.data
                is Outcome.Failure -> throw InvalidField(
                    when (val err = result.error) {
                        is AppError.InvalidResponse -> err.field
                        else -> "items"
                    }
                )
            }
        }
        PageResult(
            items = mappedItems,
            page = dto.page,
            totalPages = dto.totalPages,
            totalElements = dto.totalElements
        )
    }

    fun statusToApiQuery(status: WorkOrderStatus?): String? {
        return when (status) {
            null, WorkOrderStatus.UNKNOWN -> null
            WorkOrderStatus.OPEN -> "OPEN"
            WorkOrderStatus.ASSIGNED -> "ASSIGNED"
            WorkOrderStatus.IN_PROGRESS -> "IN_PROGRESS"
            WorkOrderStatus.COMPLETED -> "COMPLETED"
            WorkOrderStatus.CANCELLED -> "CANCELLED"
        }
    }

    private class InvalidField(val field: String) : RuntimeException("Invalid field: $field")

    private inline fun <T> field(name: String, build: () -> T): T {
        try {
            return build()
        } catch (_: IllegalArgumentException) {
            throw InvalidField(name)
        } catch (_: DateTimeParseException) {
            throw InvalidField(name)
        }
    }

    private inline fun <T> mapping(build: () -> T): Outcome<T> {
        return try {
            Outcome.Success(build())
        } catch (e: InvalidField) {
            Outcome.Failure(AppError.InvalidResponse(e.field))
        }
    }
}
