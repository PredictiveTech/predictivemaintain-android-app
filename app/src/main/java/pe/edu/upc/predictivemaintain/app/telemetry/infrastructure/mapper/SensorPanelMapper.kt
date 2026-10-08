package pe.edu.upc.predictivemaintain.app.telemetry.infrastructure.mapper

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.telemetry.domain.entity.SensorPanelItem
import pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject.CommunicationStatus
import pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject.RangeStatus
import pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject.SensorId
import pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject.SensorMetric
import pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject.SensorReading
import pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject.Threshold
import pe.edu.upc.predictivemaintain.app.telemetry.infrastructure.remote.SensorItemDto
import java.time.Instant
import java.time.format.DateTimeParseException

object SensorPanelMapper {

    fun toDomain(dtoList: List<SensorItemDto>): Outcome<List<SensorPanelItem>> = mapping {
        dtoList.map { dto ->
            SensorPanelItem(
                id = field("id") { SensorId(dto.id) },
                metric = SensorMetric.fromString(dto.metric),
                unit = field("unit") {
                    require(dto.unit.isNotBlank()) { "Unit cannot be blank" }
                    dto.unit
                },
                communication = CommunicationStatus.fromString(dto.communication),
                latestReading = dto.latestReading?.let { readingDto ->
                    SensorReading(
                        value = readingDto.value,
                        measuredAt = field("measuredAt") { Instant.parse(readingDto.measuredAt) }
                    )
                },
                threshold = dto.threshold?.let { thresholdDto ->
                    Threshold(
                        lowerBound = thresholdDto.lowerBound,
                        upperBound = thresholdDto.upperBound
                    )
                },
                rangeStatus = RangeStatus.fromString(dto.rangeStatus)
            )
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
