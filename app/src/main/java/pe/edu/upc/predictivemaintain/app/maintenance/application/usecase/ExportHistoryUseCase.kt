package pe.edu.upc.predictivemaintain.app.maintenance.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.ExportedReport
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.ReportRepository
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.ReportPeriod
import javax.inject.Inject

class ExportHistoryUseCase @Inject constructor(
    private val reportRepository: ReportRepository
) {
    suspend operator fun invoke(assetIds: List<String>, period: ReportPeriod): Outcome<ExportedReport> =
        reportRepository.exportHistory(assetIds, period)
}
