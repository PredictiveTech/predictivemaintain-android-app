package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.implementation

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.network.ErrorParser
import pe.edu.upc.predictivemaintain.app.core.network.safeApiCall
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetAvailabilityReport
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetComparisonRow
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.ExportedReport
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.ReportRepository
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.ReportPeriod
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.local.CsvFileSaver
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.mapper.ReportMapper
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.ReportApiService
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val apiService: ReportApiService,
    private val errorParser: ErrorParser,
    private val csvFileSaver: CsvFileSaver
) : ReportRepository {

    override suspend fun getAvailability(assetId: String, period: ReportPeriod): Outcome<AssetAvailabilityReport> =
        when (val result = safeApiCall(errorParser) {
            apiService.getAvailability(assetId, period.fromParam(), period.toParam())
        }) {
            is Outcome.Success -> ReportMapper.toDomain(result.data)
            is Outcome.Failure -> result
        }

    override suspend fun compareAssets(assetIds: List<String>, period: ReportPeriod): Outcome<List<AssetComparisonRow>> =
        when (val result = safeApiCall(errorParser) {
            apiService.compare(assetIds.joinToString(","), period.fromParam(), period.toParam())
        }) {
            is Outcome.Success -> ReportMapper.toDomain(result.data)
            is Outcome.Failure -> result
        }

    override suspend fun exportHistory(assetIds: List<String>, period: ReportPeriod): Outcome<ExportedReport> =
        when (val result = safeApiCall(errorParser) {
            apiService.exportHistory(assetIds.joinToString(","), period.fromParam(), period.toParam())
        }) {
            is Outcome.Success -> csvFileSaver.save(historyFileName(period), result.data)
            is Outcome.Failure -> result
        }

    internal companion object {
        fun historyFileName(period: ReportPeriod, zone: ZoneId = ZoneId.systemDefault()): String {
            val from = period.from.atZone(zone).toLocalDate()
            val to = period.to.atZone(zone).toLocalDate()
            return "maintenance-history_${from}_$to.csv"
        }
    }

    private fun ReportPeriod.fromParam() = from.truncatedTo(ChronoUnit.SECONDS).toString()
    private fun ReportPeriod.toParam() = to.truncatedTo(ChronoUnit.SECONDS).toString()
}
