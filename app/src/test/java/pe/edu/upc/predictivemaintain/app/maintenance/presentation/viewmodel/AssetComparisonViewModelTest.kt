package pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.paging.PageResult
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.CompareAssetsUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.ExportHistoryUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.ListAssetsUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Asset
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetComparisonRow
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.ExportedReport
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetCode
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetStatus
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.ComparisonSort
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.Criticality
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.DateRange
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.ReportRangeError
import pe.edu.upc.predictivemaintain.app.maintenance.fakes.FakeAssetRepository
import pe.edu.upc.predictivemaintain.app.maintenance.fakes.FakeReportRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class AssetComparisonViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val clock = Clock.fixed(Instant.parse("2026-11-23T15:30:00Z"), ZoneOffset.UTC)
    private lateinit var assets: FakeAssetRepository
    private lateinit var reports: FakeReportRepository

    private fun asset(n: Int, active: Boolean = true) = Asset(
        id = AssetId("a-$n"), code = AssetCode("AST-$n"), name = "Asset $n", location = null,
        productionLine = null, assetType = "PUMP", criticality = Criticality.HIGH, active = active,
        status = AssetStatus.OPERATIONAL, sensorTypes = emptyList()
    )

    private fun row(code: String, availability: Double) =
        AssetComparisonRow("id-$code", code, "Name $code", 1, 0, 1, 60, availability)

    private val exported = ExportedReport("content://downloads/1", "maintenance-history.csv", "text/csv")

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        assets = FakeAssetRepository()
        assets.getAssetsResult = Outcome.Success(PageResult((1..3).map { asset(it) } + asset(4, active = false), 0, 1, 4L))
        reports = FakeReportRepository()
        reports.exportResult = Outcome.Success(exported)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = AssetComparisonViewModel(
        ListAssetsUseCase(assets), CompareAssetsUseCase(reports), ExportHistoryUseCase(reports), clock
    ).also { dispatcher.scheduler.advanceUntilIdle() }

    @Test
    fun `loads the active assets and defaults to the last 30 days`() = runTest {
        val vm = viewModel()

        assertEquals(listOf("a-1", "a-2", "a-3"), vm.uiState.value.assets.map { it.id.value })
        assertEquals(DateRange.lastDays(LocalDate.of(2026, 11, 23)), vm.uiState.value.range)
        assertFalse(vm.uiState.value.canRun)
    }

    @Test
    fun `reads every page of assets`() = runTest {
        assets.getAssetsResult = Outcome.Success(PageResult(listOf(asset(1)), 0, 1, 1L))
        val vm = viewModel()

        assertEquals(1, vm.uiState.value.assets.size)
    }

    @Test
    fun `an asset list failure is shown and can be retried`() = runTest {
        assets.getAssetsResult = Outcome.Failure(AppError.Network())
        val vm = viewModel()
        assertTrue(vm.uiState.value.assetsError!!.contains("Network"))

        assets.getAssetsResult = Outcome.Success(PageResult(listOf(asset(1)), 0, 1, 1L))
        vm.loadAssets()
        dispatcher.scheduler.advanceUntilIdle()

        assertNull(vm.uiState.value.assetsError)
        assertEquals(1, vm.uiState.value.assets.size)
    }

    @Test
    fun `toggling selects and unselects`() = runTest {
        val vm = viewModel()

        vm.toggleAsset("a-1")
        vm.toggleAsset("a-2")
        vm.toggleAsset("a-1")

        assertEquals(setOf("a-2"), vm.uiState.value.selectedIds)
        assertTrue(vm.uiState.value.canRun)
    }

    @Test
    fun `selection stops at 20 assets`() = runTest {
        val vm = viewModel()

        (1..21).forEach { vm.toggleAsset("x-$it") }

        assertEquals(20, vm.uiState.value.selectedIds.size)
        assertTrue(vm.uiState.value.maxSelectionReached)
        vm.toggleAsset("x-1")
        assertFalse(vm.uiState.value.maxSelectionReached)
    }

    @Test
    fun `clearing the selection empties it`() = runTest {
        val vm = viewModel()
        vm.toggleAsset("a-1")

        vm.clearSelection()

        assertTrue(vm.uiState.value.selectedIds.isEmpty())
    }

    @Test
    fun `compare does nothing without a selection`() = runTest {
        val vm = viewModel()

        vm.compare()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(reports.comparisonCalls.isEmpty())
    }

    @Test
    fun `compare sends the selected ids and shows the worst availability first`() = runTest {
        reports.comparisonResult = Outcome.Success(listOf(row("A", 99.0), row("B", 80.0), row("C", 95.0)))
        val vm = viewModel()
        vm.toggleAsset("a-1")
        vm.toggleAsset("a-3")

        vm.compare()
        dispatcher.scheduler.advanceUntilIdle()

        val (ids, period) = reports.comparisonCalls.single()
        assertEquals(listOf("a-1", "a-3"), ids)
        assertEquals(Instant.parse("2026-10-24T00:00:00Z"), period.from)
        assertEquals(listOf("B", "C", "A"), vm.uiState.value.rows!!.map { it.assetCode })
        assertFalse(vm.uiState.value.isComparing)
    }

    @Test
    fun `changing the sort reorders the rows without calling the server`() = runTest {
        reports.comparisonResult = Outcome.Success(listOf(row("A", 99.0), row("B", 80.0)))
        val vm = viewModel()
        vm.toggleAsset("a-1")
        vm.compare()
        dispatcher.scheduler.advanceUntilIdle()

        vm.onSortChange(ComparisonSort.STOPS)

        assertEquals(ComparisonSort.STOPS, vm.uiState.value.sort)
        assertEquals(listOf("A", "B"), vm.uiState.value.rows!!.map { it.assetCode }) // tie on stops -> code
        assertEquals(1, reports.comparisonCalls.size)
    }

    @Test
    fun `a chosen sort is kept for the next comparison`() = runTest {
        reports.comparisonResult = Outcome.Success(listOf(row("A", 99.0), row("B", 80.0)))
        val vm = viewModel()
        vm.toggleAsset("a-1")
        vm.onSortChange(ComparisonSort.ALERTS)

        vm.compare()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(ComparisonSort.ALERTS, vm.uiState.value.sort)
    }

    @Test
    fun `a compare failure shows the message and keeps no rows`() = runTest {
        reports.comparisonResult = Outcome.Failure(AppError.Api(400, "TOO_MANY", "Up to 20 assets"))
        val vm = viewModel()
        vm.toggleAsset("a-1")

        vm.compare()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("Up to 20 assets", vm.uiState.value.errorMessage)
        assertNull(vm.uiState.value.rows)
    }

    @Test
    fun `an invalid range blocks compare and export`() = runTest {
        val vm = viewModel()
        vm.toggleAsset("a-1")
        vm.onRangeChange(DateRange(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 1)))

        vm.compare()
        vm.export()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(reports.comparisonCalls.isEmpty())
        assertTrue(reports.exportCalls.isEmpty())
        assertEquals(ReportRangeError.START_AFTER_END, vm.uiState.value.rangeError)
    }

    @Test
    fun `export sends the selected ids and exposes the saved file until it is shared`() = runTest {
        val vm = viewModel()
        vm.toggleAsset("a-2")
        vm.toggleAsset("a-3")

        vm.export()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("a-2", "a-3"), reports.exportCalls.single().first)
        assertEquals(exported, vm.uiState.value.exported)

        vm.onExportShared()
        assertNull(vm.uiState.value.exported)
    }

    @Test
    fun `export without a selection does nothing`() = runTest {
        val vm = viewModel()

        vm.export()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(reports.exportCalls.isEmpty())
    }

    @Test
    fun `an export failure is reported separately from the table`() = runTest {
        reports.comparisonResult = Outcome.Success(listOf(row("A", 99.0)))
        reports.exportResult = Outcome.Failure(AppError.Api(404, "NOTHING_TO_EXPORT", "Nothing to export"))
        val vm = viewModel()
        vm.toggleAsset("a-1")
        vm.compare()
        dispatcher.scheduler.advanceUntilIdle()

        vm.export()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("Nothing to export", vm.uiState.value.exportError)
        assertEquals(1, vm.uiState.value.rows!!.size)
        assertFalse(vm.uiState.value.isExporting)
    }
}
