package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.mapper

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Evidence
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.EvidenceId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderId
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.EvidenceDto
import java.time.Instant
import java.time.format.DateTimeParseException

object EvidenceMapper {

    fun toDomain(dto: EvidenceDto): Outcome<Evidence> {
        val id = runCatching { EvidenceId(dto.id) }.getOrNull()
            ?: return Outcome.Failure(AppError.InvalidResponse("id"))
        val workOrderId = runCatching { WorkOrderId(dto.workOrderId) }.getOrNull()
            ?: return Outcome.Failure(AppError.InvalidResponse("workOrderId"))
        val uploadedAt = try {
            Instant.parse(dto.uploadedAt)
        } catch (_: DateTimeParseException) {
            return Outcome.Failure(AppError.InvalidResponse("uploadedAt"))
        }
        return Outcome.Success(Evidence(id, workOrderId, dto.mimeType, uploadedAt))
    }

    fun toDomain(dtos: List<EvidenceDto>): Outcome<List<Evidence>> {
        val result = ArrayList<Evidence>(dtos.size)
        for (dto in dtos) {
            when (val mapped = toDomain(dto)) {
                is Outcome.Success -> result.add(mapped.data)
                is Outcome.Failure -> return mapped
            }
        }
        return Outcome.Success(result)
    }
}
