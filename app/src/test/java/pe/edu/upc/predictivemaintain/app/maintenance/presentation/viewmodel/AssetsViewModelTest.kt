package pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel

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
import pe.edu.upc.predictivemaintain.app.core.paging.PageResult
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.ListAssetsUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Asset
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetCode
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetStatus
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.Criticality
import pe.edu.upc.predictivemaintain.app.maintenance.fakes.FakeAssetRepository

@OptIn(ExperimentalCoroutinesApi::class)
class AssetsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeAssetRepository
    private lateinit var listAssetsUseCase: ListAssetsUseCase
    private lateinit var viewModel: AssetsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAssetRepository()
        listAssetsUseCase = ListAssetsUseCase(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `successful load updates items and page state`() = runTest {
        val asset = Asset(
            id = AssetId("1"),
            code = AssetCode("C-1"),
            name = "Pump",
            location = "Plant A",
            productionLine = "Line 1",
            assetType = "ROTATING",
            criticality = Criticality.HIGH,
            active = true,
            status = AssetStatus.OPERATIONAL,
            sensorTypes = emptyList()
        )
        fakeRepository.getAssetsResult = Outcome.Success(PageResult(listOf(asset), 0, 1, 1L))

        viewModel = AssetsViewModel(listAssetsUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals("Pump", state.items[0].name)
        assertEquals(0, state.page)
        assertEquals(false, state.hasMore)
    }

    @Test
    fun `failed refresh keeps existing items and sets error message`() = runTest {
        val asset = Asset(
            id = AssetId("1"),
            code = AssetCode("C-1"),
            name = "Pump",
            location = "Plant A",
            productionLine = "Line 1",
            assetType = "ROTATING",
            criticality = Criticality.HIGH,
            active = true,
            status = AssetStatus.OPERATIONAL,
            sensorTypes = emptyList()
        )
        fakeRepository.getAssetsResult = Outcome.Success(PageResult(listOf(asset), 0, 1, 1L))
        viewModel = AssetsViewModel(listAssetsUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        fakeRepository.getAssetsResult = Outcome.Failure(AppError.Network())
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertTrue(state.errorMessage != null)
    }

    @Test
    fun `load more appends items`() = runTest {
        val asset1 = Asset(
            id = AssetId("1"),
            code = AssetCode("C-1"),
            name = "Pump 1",
            location = "Plant A",
            productionLine = "Line 1",
            assetType = "ROTATING",
            criticality = Criticality.HIGH,
            active = true,
            status = AssetStatus.OPERATIONAL,
            sensorTypes = emptyList()
        )
        val asset2 = Asset(
            id = AssetId("2"),
            code = AssetCode("C-2"),
            name = "Pump 2",
            location = "Plant A",
            productionLine = "Line 1",
            assetType = "ROTATING",
            criticality = Criticality.HIGH,
            active = true,
            status = AssetStatus.OPERATIONAL,
            sensorTypes = emptyList()
        )

        fakeRepository.getAssetsResult = Outcome.Success(PageResult(listOf(asset1), 0, 2, 2L))
        viewModel = AssetsViewModel(listAssetsUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        fakeRepository.getAssetsResult = Outcome.Success(PageResult(listOf(asset2), 1, 2, 2L))
        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.items.size)
        assertEquals("Pump 1", state.items[0].name)
        assertEquals("Pump 2", state.items[1].name)
        assertEquals(1, state.page)
    }
}
