package pe.edu.upc.predictivemaintain.app.telemetry.presentation.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.telemetry.application.usecase.GetSensorPanelUseCase
import pe.edu.upc.predictivemaintain.app.telemetry.domain.entity.SensorPanelItem
import pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject.CommunicationStatus
import pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject.RangeStatus
import pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject.SensorId
import pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject.SensorMetric
import pe.edu.upc.predictivemaintain.app.telemetry.fakes.FakeSensorPanelRepository

@OptIn(ExperimentalCoroutinesApi::class)
class SensorPanelViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeSensorPanelRepository
    private lateinit var getSensorPanelUseCase: GetSensorPanelUseCase
    private lateinit var viewModel: SensorPanelViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeSensorPanelRepository()
        getSensorPanelUseCase = GetSensorPanelUseCase(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `refresh replaces data successfully`() = runTest {
        val item = SensorPanelItem(
            id = SensorId("s-1"),
            metric = SensorMetric.VIBRATION,
            unit = "Hz",
            communication = CommunicationStatus.ONLINE,
            latestReading = null,
            threshold = null,
            rangeStatus = RangeStatus.NORMAL
        )
        fakeRepository.getSensorPanelResult = Outcome.Success(listOf(item))

        viewModel = SensorPanelViewModel(getSensorPanelUseCase)
        viewModel.loadSensors("asset-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals("s-1", state.items[0].id.value)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `failure keeps previous data and sets error message`() = runTest {
        val item = SensorPanelItem(
            id = SensorId("s-1"),
            metric = SensorMetric.VIBRATION,
            unit = "Hz",
            communication = CommunicationStatus.ONLINE,
            latestReading = null,
            threshold = null,
            rangeStatus = RangeStatus.NORMAL
        )
        fakeRepository.getSensorPanelResult = Outcome.Success(listOf(item))
        viewModel = SensorPanelViewModel(getSensorPanelUseCase)
        viewModel.loadSensors("asset-1")
        testDispatcher.scheduler.advanceUntilIdle()

        fakeRepository.getSensorPanelResult = Outcome.Failure(AppError.Network())
        viewModel.loadSensors("asset-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertTrue(state.errorMessage != null)
    }
}
