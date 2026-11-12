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
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.DeactivateAssetUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.GetAssetUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetDetail
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetCode
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.Criticality
import pe.edu.upc.predictivemaintain.app.maintenance.fakes.FakeAssetRepository
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.CreateAssetError
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.DeactivateDialog

@OptIn(ExperimentalCoroutinesApi::class)
class AssetDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeAssetRepository
    private lateinit var viewModel: AssetDetailViewModel

    private val active = AssetDetail(
        id = AssetId("a-1"), code = AssetCode("P-001"), name = "Pump", location = "Plant A",
        productionLine = null, assetType = "PUMP", criticality = Criticality.HIGH,
        latitude = null, longitude = null, active = true
    )
    private val openOrders = AppError.Api(409, "ASSET_HAS_OPEN_WORK_ORDERS", "2 open orders")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeAssetRepository()
        repository.getAssetResult = Outcome.Success(active)
        repository.deactivateAssetResult = Outcome.Success(active.copy(active = false))
        viewModel = AssetDetailViewModel(GetAssetUseCase(repository), DeactivateAssetUseCase(repository))
        viewModel.loadAsset("a-1")
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun confirm() {
        viewModel.confirmDeactivate()
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `requesting deactivation only opens the dialog and calls nothing`() = runTest {
        viewModel.requestDeactivate()

        assertEquals(DeactivateDialog.CONFIRM, viewModel.uiState.value.deactivateDialog)
        assertTrue(repository.deactivateCalls.isEmpty())
    }

    @Test
    fun `dismissing the dialog cancels the deactivation`() = runTest {
        viewModel.requestDeactivate()
        viewModel.dismissDeactivateDialog()
        confirm()

        assertEquals(DeactivateDialog.NONE, viewModel.uiState.value.deactivateDialog)
        assertTrue(repository.deactivateCalls.isEmpty())
    }

    @Test
    fun `confirming deactivates without force and shows the asset as inactive`() = runTest {
        viewModel.requestDeactivate()
        confirm()

        assertEquals(listOf("a-1" to false), repository.deactivateCalls)
        val state = viewModel.uiState.value
        assertFalse(state.asset!!.active)
        assertTrue(state.deactivated)
        assertFalse(state.isDeactivating)
        assertNull(state.deactivateError)
        assertEquals(DeactivateDialog.NONE, state.deactivateDialog)
    }

    @Test
    fun `open work orders warning asks again and then retries with force`() = runTest {
        repository.deactivateAssetResult = Outcome.Failure(openOrders)
        viewModel.requestDeactivate()
        confirm()

        var state = viewModel.uiState.value
        assertEquals(DeactivateDialog.CONFIRM_WITH_OPEN_ORDERS, state.deactivateDialog)
        assertTrue(state.asset!!.active)
        assertFalse(state.deactivated)
        assertNull(state.deactivateError)

        repository.deactivateAssetResult = Outcome.Success(active.copy(active = false))
        confirm()

        assertEquals(listOf("a-1" to false, "a-1" to true), repository.deactivateCalls)
        state = viewModel.uiState.value
        assertFalse(state.asset!!.active)
        assertTrue(state.deactivated)
    }

    @Test
    fun `declining the open work orders warning keeps the asset active`() = runTest {
        repository.deactivateAssetResult = Outcome.Failure(openOrders)
        viewModel.requestDeactivate()
        confirm()
        viewModel.dismissDeactivateDialog()
        confirm()

        assertEquals(1, repository.deactivateCalls.size)
        assertTrue(viewModel.uiState.value.asset!!.active)
    }

    @Test
    fun `a 409 after forcing is shown as an error instead of looping`() = runTest {
        repository.deactivateAssetResult = Outcome.Failure(openOrders)
        viewModel.requestDeactivate()
        confirm()
        confirm()

        val state = viewModel.uiState.value
        assertEquals(DeactivateDialog.NONE, state.deactivateDialog)
        assertEquals(CreateAssetError.HasOpenWorkOrders, state.deactivateError)
        assertEquals(listOf("a-1" to false, "a-1" to true), repository.deactivateCalls)
    }

    @Test
    fun `403 and 404 are exposed as specific errors`() = runTest {
        repository.deactivateAssetResult = Outcome.Failure(AppError.Api(403, "FORBIDDEN", "no"))
        viewModel.requestDeactivate()
        confirm()
        assertEquals(CreateAssetError.Forbidden, viewModel.uiState.value.deactivateError)
        assertTrue(viewModel.uiState.value.asset!!.active)

        repository.deactivateAssetResult = Outcome.Failure(AppError.Api(404, "ASSET_NOT_FOUND", "gone"))
        viewModel.requestDeactivate()
        confirm()
        assertEquals(CreateAssetError.NotFound, viewModel.uiState.value.deactivateError)
        assertFalse(viewModel.uiState.value.deactivated)
    }

    @Test
    fun `network failure is exposed and a new request clears it`() = runTest {
        repository.deactivateAssetResult = Outcome.Failure(AppError.Network())
        viewModel.requestDeactivate()
        confirm()
        assertTrue((viewModel.uiState.value.deactivateError as CreateAssetError.Other).error is AppError.Network)

        viewModel.requestDeactivate()
        assertNull(viewModel.uiState.value.deactivateError)
    }

    @Test
    fun `an already inactive asset cannot be deactivated again`() = runTest {
        repository.getAssetResult = Outcome.Success(active.copy(active = false))
        viewModel.loadAsset("a-1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.requestDeactivate()

        assertEquals(DeactivateDialog.NONE, viewModel.uiState.value.deactivateDialog)
    }

    @Test
    fun `confirm without an open dialog does nothing`() = runTest {
        confirm()

        assertTrue(repository.deactivateCalls.isEmpty())
    }
}
