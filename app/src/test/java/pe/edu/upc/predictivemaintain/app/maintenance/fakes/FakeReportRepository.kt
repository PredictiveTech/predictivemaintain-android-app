package pe.edu.upc.predictivemaintain.app.maintenance.fakes

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetAvailabilityReport
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetComparisonRow
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.ExportedReport
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.ReportRepository
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.ReportPeriod

class FakeReportRepository : ReportRepository {
    var availabilityResult: Outcome<AssetAvailabilityReport> = Outcome.Failure(AppError.Unknown())
    var comparisonResult: Outcome<List<AssetComparisonRow>> = Outcome.Success(emptyList())
    var exportResult: Outcome<ExportedReport> = Outcome.Failure(AppError.Unknown())

    val availabilityCalls = mutableListOf<Pair<String, ReportPeriod>>()
    val comparisonCalls = mutableListOf<Pair<List<String>, ReportPeriod>>()
    val exportCalls = mutableListOf<Pair<List<String>, ReportPeriod>>()

    override suspend fun getAvailability(assetId: String, period: ReportPeriod): Outcome<AssetAvailabilityReport> {
        availabilityCalls += assetId to period
        return availabilityResult
    }

    override suspend fun compareAssets(assetIds: List<String>, period: ReportPeriod): Outcome<List<AssetComparisonRow>> {
        comparisonCalls += assetIds to period
        return comparisonResult
    }

    override suspend fun exportHistory(assetIds: List<String>, period: ReportPeriod): Outcome<ExportedReport> {
        exportCalls += assetIds to period
        return exportResult
    }
}
