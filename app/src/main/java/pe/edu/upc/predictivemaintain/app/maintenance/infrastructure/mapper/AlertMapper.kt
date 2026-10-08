package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.mapper

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.paging.PageResult
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Alert
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertDiagnostic
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertSeverity
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertStatus
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.AlertDto
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.AlertPageDto
import java.time.Instant
import java.time.format.DateTimeParseException

object AlertMapper {

    fun toDomain(dto: AlertDto): Outcome<Alert> = mapping {
        Alert(
            id = field("id") { AlertId(dto.id) },
            assetId = field("assetId") {
                require(dto.assetId.isNotBlank()) { "AssetId cannot be blank" }
                dto.assetId
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
            status = AlertStatus.fromString(dto.status),
            raisedAt = field("raisedAt") { Instant.parse(dto.raisedAt) },
            discardReason = dto.discardReason,
            version = dto.version,
            diagnostic = dto.diagnostic?.let { diagDto ->
                AlertDiagnostic(
                    metric = field("diagnostic.metric") {
                        require(diagDto.metric.isNotBlank()) { "Metric cannot be blank" }
                        diagDto.metric
                    },
                    unit = field("diagnostic.unit") {
                        require(diagDto.unit.isNotBlank()) { "Unit cannot be blank" }
                        diagDto.unit
                    },
                    observedValue = diagDto.observedValue,
                    lowerBound = diagDto.lowerBound,
                    upperBound = diagDto.upperBound,
                    measuredAt = field("diagnostic.measuredAt") { Instant.parse(diagDto.measuredAt) }
                )
            }
        )
    }

    fun toDomain(dto: AlertPageDto): Outcome<PageResult<Alert>> = mapping {
        val mappedItems = dto.items.map { itemDto ->
            when (val result = toDomain(itemDto)) {
                is Outcome.Success -> result.data
                is Outcome.Failure -> throw InvalidField(when (val err = result.error) {
                    is AppError.InvalidResponse -> err.field
                    else -> "items"
                })
            }
        }
        PageResult(
            items = mappedItems,
            page = dto.page,
            totalPages = dto.totalPages,
            totalElements = dto.totalElements
        )
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
