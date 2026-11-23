package pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject

import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetComparisonRow

/** Orders of the comparison table. Every order puts the worst performer first. */
enum class ComparisonSort(private val primary: Comparator<AssetComparisonRow>) {
    AVAILABILITY(compareBy { it.availabilityPercent }),
    DOWNTIME(compareByDescending { it.downtimeSeconds }),
    ALERTS(compareByDescending { it.alerts }),
    CRITICAL_ALERTS(compareByDescending { it.criticalAlerts }),
    STOPS(compareByDescending { it.stops });

    fun sort(rows: List<AssetComparisonRow>): List<AssetComparisonRow> =
        rows.sortedWith(primary.thenBy { it.assetCode })
}
