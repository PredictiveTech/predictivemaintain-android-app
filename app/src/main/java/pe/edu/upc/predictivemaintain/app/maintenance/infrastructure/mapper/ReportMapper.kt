package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.mapper

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetAvailabilityReport
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetComparisonRow
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetEvent
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetEventType
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.AssetEventDto
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.AvailabilityReportDto
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.ComparisonRowDto
import java.time.Instant
import java.time.format.DateTimeParseException

object ReportMapper {

    fun toDomain(dto: AvailabilityReportDto): Outcome<AssetAvailabilityReport> = mapping {
        val events = dto.events.map(::event)
        AssetAvailabilityReport(
            assetId = text("assetId", dto.assetId),
            assetCode = text("assetCode", dto.assetCode),
            from = instant("from", dto.from),
            to = instant("to", dto.to),
            availabilityPercent = percent("availabilityPercent", dto.availabilityPercent),
            downtimeSeconds = nonNegative("downtimeSeconds", dto.downtimeSeconds),
            periodSeconds = nonNegative("periodSeconds", dto.periodSeconds),
            // Newest first: the manager looks at what happened last.
            events = events.sortedByDescending { it.occurredAt }
        )
    }

    fun toDomain(dtos: List<ComparisonRowDto>): Outcome<List<AssetComparisonRow>> = mapping {
        dtos.map { dto ->
            AssetComparisonRow(
                assetId = text("assetId", dto.assetId),
                assetCode = text("assetCode", dto.assetCode),
                assetName = dto.assetName.orEmpty(),
                alerts = nonNegative("alerts", dto.alerts?.toLong()).toInt(),
                criticalAlerts = nonNegative("criticalAlerts", dto.criticalAlerts?.toLong()).toInt(),
                stops = nonNegative("stops", dto.stops?.toLong()).toInt(),
                downtimeSeconds = nonNegative("downtimeSeconds", dto.downtimeSeconds),
                availabilityPercent = percent("availabilityPercent", dto.availabilityPercent)
            )
        }
    }

    private fun event(dto: AssetEventDto) = AssetEvent(
        type = when (dto.type?.uppercase()) {
            "ALERT" -> AssetEventType.ALERT
            "DOWNTIME" -> AssetEventType.DOWNTIME
            else -> AssetEventType.UNKNOWN
        },
        referenceId = text("events.referenceId", dto.referenceId),
        occurredAt = instant("events.occurredAt", dto.occurredAt),
        endedAt = dto.endedAt?.let { instant("events.endedAt", it) },
        severity = dto.severity,
        status = dto.status
    )

    private class InvalidField(val field: String) : RuntimeException("Invalid field: $field")

    private fun text(name: String, value: String?): String =
        value?.takeIf { it.isNotBlank() } ?: throw InvalidField(name)

    private fun instant(name: String, value: String?): Instant = try {
        Instant.parse(value ?: throw InvalidField(name))
    } catch (_: DateTimeParseException) {
        throw InvalidField(name)
    }

    private fun nonNegative(name: String, value: Long?): Long =
        value?.takeIf { it >= 0 } ?: throw InvalidField(name)

    private fun percent(name: String, value: Double?): Double =
        value?.takeIf { it.isFinite() && it in 0.0..100.0 } ?: throw InvalidField(name)

    private inline fun <T> mapping(build: () -> T): Outcome<T> = try {
        Outcome.Success(build())
    } catch (e: InvalidField) {
        Outcome.Failure(AppError.InvalidResponse(e.field))
    }
}
