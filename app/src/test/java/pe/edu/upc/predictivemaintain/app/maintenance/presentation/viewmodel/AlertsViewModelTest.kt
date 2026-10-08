package pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.paging.PageResult
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.ListAlertsUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Alert
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertSeverity
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertStatus
import pe.edu.upc.predictivemaintain.app.maintenance.fakes.FakeAlertRepository
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class AlertsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeAlertRepository
    private lateinit var listAlertsUseCase: ListAlertsUseCase
    private lateinit var viewModel: AlertsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAlertRepository()
        listAlertsUseCase = ListAlertsUseCase(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `successful load updates items state`() = runTest {
        val alert = Alert(
            id = AlertId("a-1"),
            assetId = "ast-1",
            assetCode = "C-1",
            assetName = "Compressor",
            severity = AlertSeverity.CRITICAL,
            status = AlertStatus.IN_REVIEW,
            raisedAt = Instant.now(),
            discardReason = null,
            version = 1L,
            diagnostic = null
        )
        fakeRepository.getAlertsResult = Outcome.Success(PageResult(listOf(alert), 0, 1, 1L))

        viewModel = AlertsViewModel(listAlertsUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals("Compressor", state.items[0].assetName)
    }

    @Test
    fun `null blank or placeholder argument yields no assetId filter`() = runTest {
        viewModel = AlertsViewModel(listAlertsUseCase)
        viewModel.setAssetIdArg(null)
        assertNull(viewModel.uiState.value.assetIdFilter)

        viewModel.setAssetIdArg("")
        assertNull(viewModel.uiState.value.assetIdFilter)

        viewModel.setAssetIdArg("   ")
        assertNull(viewModel.uiState.value.assetIdFilter)

        viewModel.setAssetIdArg("{assetId}")
        assertNull(viewModel.uiState.value.assetIdFilter)
    }

    @Test
    fun `valid UUID argument sets assetId filter`() = runTest {
        val validUuid = "123e4567-e89b-12d3-a456-426614174000"
        viewModel = AlertsViewModel(listAlertsUseCase)
        viewModel.setAssetIdArg(validUuid)
        assertEquals(validUuid, viewModel.uiState.value.assetIdFilter)
    }
}
