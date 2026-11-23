package pe.edu.upc.predictivemaintain.app.maintenance.domain.repository

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetAvailabilityReport
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetComparisonRow
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.ExportedReport
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.ReportPeriod

interface ReportRepository {
    suspend fun getAvailability(assetId: String, period: ReportPeriod): Outcome<AssetAvailabilityReport>
    suspend fun compareAssets(assetIds: List<String>, period: ReportPeriod): Outcome<List<AssetComparisonRow>>

    /** Downloads the history as CSV and saves it in the device's Downloads folder. */
    suspend fun exportHistory(assetIds: List<String>, period: ReportPeriod): Outcome<ExportedReport>
}
