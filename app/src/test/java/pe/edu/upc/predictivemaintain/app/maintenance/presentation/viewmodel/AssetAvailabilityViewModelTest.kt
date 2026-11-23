package pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.ExportHistoryUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.GetAssetAvailabilityUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetAvailabilityReport
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.ExportedReport
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.DateRange
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.ReportRangeError
import pe.edu.upc.predictivemaintain.app.maintenance.fakes.FakeReportRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class AssetAvailabilityViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val clock = Clock.fixed(Instant.parse("2026-11-23T15:30:00Z"), ZoneOffset.UTC)
    private lateinit var repository: FakeReportRepository

    private val report = AssetAvailabilityReport(
        assetId = "a-1", assetCode = "PUMP-01",
        from = Instant.parse("2026-10-24T00:00:00Z"), to = Instant.parse("2026-11-23T15:30:00Z"),
        availabilityPercent = 98.0, downtimeSeconds = 3600, periodSeconds = 180_000, events = emptyList()
    )
    private val exported = ExportedReport("content://downloads/1", "maintenance-history.csv", "text/csv")

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeReportRepository()
        repository.availabilityResult = Outcome.Success(report)
        repository.exportResult = Outcome.Success(exported)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = AssetAvailabilityViewModel(
        SavedStateHandle(mapOf(AssetAvailabilityViewModel.ARG_ASSET_ID to "a-1")),
        GetAssetAvailabilityUseCase(repository),
        ExportHistoryUseCase(repository),
        clock
    )

    @Test
    fun `loads the last 30 days when it opens`() = runTest {
        val vm = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        val (assetId, period) = repository.availabilityCalls.single()
        assertEquals("a-1", assetId)
        assertEquals(Instant.parse("2026-10-24T00:00:00Z"), period.from)
        assertEquals(Instant.parse("2026-11-23T15:30:00Z"), period.to)
        assertEquals(DateRange.lastDays(LocalDate.of(2026, 11, 23)), vm.uiState.value.range)
        assertEquals(report, vm.uiState.value.report)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `changing the range reloads with the new period`() = runTest {
        val vm = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.onRangeChange(DateRange(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 10)))
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, repository.availabilityCalls.size)
        assertEquals(Instant.parse("2026-11-01T00:00:00Z"), repository.availabilityCalls.last().second.from)
        assertEquals(Instant.parse("2026-11-11T00:00:00Z"), repository.availabilityCalls.last().second.to)
    }

    @Test
    fun `an invalid range shows the error and calls nothing`() = runTest {
        val vm = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.onRangeChange(DateRange(LocalDate.of(2025, 1, 1), LocalDate.of(2026, 11, 1)))
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, repository.availabilityCalls.size)
        assertEquals(ReportRangeError.TOO_LONG, vm.uiState.value.rangeError)
        assertNull(vm.uiState.value.report)
    }

    @Test
    fun `a failure shows the server message and clears the report`() = runTest {
        repository.availabilityResult = Outcome.Failure(AppError.Api(400, "BAD_RANGE", "Range too long"))
        val vm = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("Range too long", vm.uiState.value.errorMessage)
        assertNull(vm.uiState.value.report)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `a network failure shows a connection message`() = runTest {
        repository.availabilityResult = Outcome.Failure(AppError.Network())
        val vm = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.errorMessage!!.contains("Network"))
    }

    @Test
    fun `retry after a failure loads again`() = runTest {
        repository.availabilityResult = Outcome.Failure(AppError.Network())
        val vm = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        repository.availabilityResult = Outcome.Success(report)
        vm.load()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(report, vm.uiState.value.report)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `export asks for this asset and the current period and exposes the saved file`() = runTest {
        val vm = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.export()
        dispatcher.scheduler.advanceUntilIdle()

        val (ids, period) = repository.exportCalls.single()
        assertEquals(listOf("a-1"), ids)
        assertEquals(Instant.parse("2026-10-24T00:00:00Z"), period.from)
        assertEquals(exported, vm.uiState.value.exported)
        assertFalse(vm.uiState.value.isExporting)
    }

    @Test
    fun `sharing the export clears it so it is not shared twice`() = runTest {
        val vm = viewModel()
        dispatcher.scheduler.advanceUntilIdle()
        vm.export()
        dispatcher.scheduler.advanceUntilIdle()

        vm.onExportShared()

        assertNull(vm.uiState.value.exported)
    }

    @Test
    fun `an export failure is reported and can be dismissed`() = runTest {
        repository.exportResult = Outcome.Failure(AppError.Api(404, "NOTHING_TO_EXPORT", "Nothing to export"))
        val vm = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.export()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("Nothing to export", vm.uiState.value.exportError)
        assertNull(vm.uiState.value.exported)
        assertNotNull(vm.uiState.value.report) // the report on screen is untouched

        vm.dismissExportError()
        assertNull(vm.uiState.value.exportError)
    }

    @Test
    fun `export with an invalid range calls nothing`() = runTest {
        val vm = viewModel()
        dispatcher.scheduler.advanceUntilIdle()
        vm.onRangeChange(DateRange(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 1)))

        vm.export()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(repository.exportCalls.isEmpty())
        assertEquals(ReportRangeError.START_AFTER_END, vm.uiState.value.rangeError)
    }
}
