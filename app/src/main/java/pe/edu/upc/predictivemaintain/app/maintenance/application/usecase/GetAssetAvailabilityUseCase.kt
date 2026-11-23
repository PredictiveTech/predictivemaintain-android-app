package pe.edu.upc.predictivemaintain.app.maintenance.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetAvailabilityReport
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.ReportRepository
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.ReportPeriod
import javax.inject.Inject

class GetAssetAvailabilityUseCase @Inject constructor(
    private val reportRepository: ReportRepository
) {
    suspend operator fun invoke(assetId: String, period: ReportPeriod): Outcome<AssetAvailabilityReport> =
        reportRepository.getAvailability(assetId, period)
}
