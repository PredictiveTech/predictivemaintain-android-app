package pe.edu.upc.predictivemaintain.app.maintenance.presentation.state

import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Asset
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetAvailabilityReport
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetComparisonRow
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.ExportedReport
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.ComparisonSort
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.DateRange
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.ReportRangeError

/** US-14, US-16. */
data class AssetAvailabilityUiState(
    val range: DateRange,
    val isLoading: Boolean = false,
    val report: AssetAvailabilityReport? = null,
    val rangeError: ReportRangeError? = null,
    val errorMessage: String? = null,
    val isExporting: Boolean = false,
    val exportError: String? = null,
    /** Set when the file is saved; the screen opens the share sheet and then calls onExportShared(). */
    val exported: ExportedReport? = null
)

/** US-15, US-16. */
data class AssetComparisonUiState(
    val range: DateRange,
    val isLoadingAssets: Boolean = false,
    val assets: List<Asset> = emptyList(),
    val assetsError: String? = null,
    val selectedIds: Set<String> = emptySet(),
    val maxSelectionReached: Boolean = false,
    val isComparing: Boolean = false,
    /** Null until the first comparison; already ordered by [sort]. */
    val rows: List<AssetComparisonRow>? = null,
    val sort: ComparisonSort = ComparisonSort.AVAILABILITY,
    val rangeError: ReportRangeError? = null,
    val errorMessage: String? = null,
    val isExporting: Boolean = false,
    val exportError: String? = null,
    val exported: ExportedReport? = null
) {
    val canRun: Boolean get() = selectedIds.isNotEmpty() && !isComparing && !isExporting
}
