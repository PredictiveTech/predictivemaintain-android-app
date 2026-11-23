package pe.edu.upc.predictivemaintain.app.maintenance.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetComparisonRow
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.ReportRepository
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.ReportPeriod
import javax.inject.Inject

class CompareAssetsUseCase @Inject constructor(
    private val reportRepository: ReportRepository
) {
    suspend operator fun invoke(assetIds: List<String>, period: ReportPeriod): Outcome<List<AssetComparisonRow>> =
        reportRepository.compareAssets(assetIds, period)

    companion object {
        /** The backend accepts between 1 and 20 assets. */
        const val MAX_ASSETS = 20
    }
}
