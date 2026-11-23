package pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject

import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetComparisonRow
import org.junit.Assert.assertEquals
import org.junit.Test

class ComparisonSortTest {

    private fun row(code: String, availability: Double, downtime: Long, alerts: Int, critical: Int, stops: Int) =
        AssetComparisonRow("id-$code", code, "Name $code", alerts, critical, stops, downtime, availability)

    private val a = row("A", availability = 99.0, downtime = 100, alerts = 1, critical = 0, stops = 5)
    private val b = row("B", availability = 80.0, downtime = 500, alerts = 9, critical = 2, stops = 1)
    private val c = row("C", availability = 95.0, downtime = 900, alerts = 4, critical = 7, stops = 3)
    private val rows = listOf(a, b, c)

    private fun codes(sort: ComparisonSort) = sort.sort(rows).map { it.assetCode }

    @Test
    fun `availability puts the lowest first`() = assertEquals(listOf("B", "C", "A"), codes(ComparisonSort.AVAILABILITY))

    @Test
    fun `downtime puts the longest first`() = assertEquals(listOf("C", "B", "A"), codes(ComparisonSort.DOWNTIME))

    @Test
    fun `alerts puts the most first`() = assertEquals(listOf("B", "C", "A"), codes(ComparisonSort.ALERTS))

    @Test
    fun `critical alerts puts the most first`() = assertEquals(listOf("C", "B", "A"), codes(ComparisonSort.CRITICAL_ALERTS))

    @Test
    fun `stops puts the most first`() = assertEquals(listOf("A", "C", "B"), codes(ComparisonSort.STOPS))

    @Test
    fun `ties are broken by asset code`() {
        val tied = listOf(row("Z", 100.0, 0, 0, 0, 0), row("M", 100.0, 0, 0, 0, 0))

        assertEquals(listOf("M", "Z"), ComparisonSort.AVAILABILITY.sort(tied).map { it.assetCode })
    }
}
