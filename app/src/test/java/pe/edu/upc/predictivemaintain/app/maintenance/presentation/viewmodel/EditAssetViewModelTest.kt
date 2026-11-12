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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.GetAssetUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.UpdateAssetUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetDetail
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetCode
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.Criticality
import pe.edu.upc.predictivemaintain.app.maintenance.fakes.FakeAssetRepository
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.AssetFormField
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.CreateAssetError
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.FieldError

@OptIn(ExperimentalCoroutinesApi::class)
class EditAssetViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeAssetRepository

    private val existing = AssetDetail(
        id = AssetId("a-1"), code = AssetCode("P-001"), name = "Pump", location = "Plant A",
        plantId = "plant-9", productionLine = "Line 1", assetType = "PUMP",
        criticality = Criticality.HIGH, latitude = -12.5, longitude = -77.0, active = true
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeAssetRepository()
        repository.getAssetResult = Outcome.Success(existing)
        repository.updateAssetResult = Outcome.Success(existing.copy(name = "Pump 2"))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = EditAssetViewModel(
        SavedStateHandle(mapOf(EditAssetViewModel.ARG_ASSET_ID to "a-1")),
        GetAssetUseCase(repository),
        UpdateAssetUseCase(repository)
    ).also { testDispatcher.scheduler.advanceUntilIdle() }

    @Test
    fun `starts loading and then prefills every field`() = runTest {
        val viewModel = EditAssetViewModel(
            SavedStateHandle(mapOf(EditAssetViewModel.ARG_ASSET_ID to "a-1")),
            GetAssetUseCase(repository),
            UpdateAssetUseCase(repository)
        )
        assertTrue(viewModel.uiState.value.isLoading)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("P-001", state.code)
        assertEquals("Pump", state.name)
        assertEquals("Plant A", state.location)
        assertEquals("PUMP", state.assetType)
        assertEquals(Criticality.HIGH, state.criticality)
        assertEquals("Line 1", state.productionLine)
        assertEquals("-12.5", state.latitude)
        assertEquals("-77.0", state.longitude)
        assertEquals("plant-9", state.plantId)
    }

    @Test
    fun `load failure exposes loadError and retry recovers`() = runTest {
        repository.getAssetResult = Outcome.Failure(AppError.Network())
        val viewModel = createViewModel()
        assertTrue(viewModel.uiState.value.loadError is AppError.Network)

        repository.getAssetResult = Outcome.Success(existing)
        viewModel.load()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.loadError)
        assertEquals("Pump", viewModel.uiState.value.name)
    }

    @Test
    fun `submit while load failed does nothing`() = runTest {
        repository.getAssetResult = Outcome.Failure(AppError.Network())
        val viewModel = createViewModel()

        viewModel.submit()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(repository.updatedRequests.isEmpty())
    }

    @Test
    fun `the code cannot be edited`() = runTest {
        val viewModel = createViewModel()

        viewModel.onFieldChange(AssetFormField.CODE, "OTHER")

        assertEquals("P-001", viewModel.uiState.value.code)
    }

    @Test
    fun `save sends changes with the original plantId and marks saved`() = runTest {
        val viewModel = createViewModel()
        viewModel.onFieldChange(AssetFormField.NAME, "  Pump 2 ")
        viewModel.onCriticalityChange(Criticality.CRITICAL)
        viewModel.submit()
        testDispatcher.scheduler.advanceUntilIdle()

        val (id, changes) = repository.updatedRequests.single()
        assertEquals("a-1", id)
        assertEquals("Pump 2", changes.name)
        assertEquals(Criticality.CRITICAL, changes.criticality)
        assertEquals("plant-9", changes.plantId)
        assertEquals(-12.5, changes.latitude!!, 0.0)
        assertTrue(viewModel.uiState.value.saved)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun `clearing optional fields sends nulls`() = runTest {
        val viewModel = createViewModel()
        viewModel.onFieldChange(AssetFormField.PRODUCTION_LINE, "")
        viewModel.onFieldChange(AssetFormField.LATITUDE, "")
        viewModel.onFieldChange(AssetFormField.LONGITUDE, "")
        viewModel.submit()
        testDispatcher.scheduler.advanceUntilIdle()

        val changes = repository.updatedRequests.single().second
        assertNull(changes.productionLine)
        assertNull(changes.latitude)
        assertNull(changes.longitude)
    }

    @Test
    fun `blank required fields are rejected without calling the server`() = runTest {
        val viewModel = createViewModel()
        viewModel.onFieldChange(AssetFormField.NAME, " ")
        viewModel.onFieldChange(AssetFormField.LOCATION, "")
        viewModel.submit()
        testDispatcher.scheduler.advanceUntilIdle()

        val errors = viewModel.uiState.value.fieldErrors
        assertEquals(FieldError.REQUIRED, errors[AssetFormField.NAME])
        assertEquals(FieldError.REQUIRED, errors[AssetFormField.LOCATION])
        assertFalse(AssetFormField.CODE in errors)
        assertTrue(repository.updatedRequests.isEmpty())
    }

    @Test
    fun `incomplete or out of range coordinates are rejected`() = runTest {
        val viewModel = createViewModel()
        viewModel.onFieldChange(AssetFormField.LONGITUDE, "")
        viewModel.submit()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(FieldError.COORDINATES_INCOMPLETE, viewModel.uiState.value.fieldErrors[AssetFormField.LONGITUDE])

        viewModel.onFieldChange(AssetFormField.LATITUDE, "95")
        viewModel.onFieldChange(AssetFormField.LONGITUDE, "10")
        viewModel.submit()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(FieldError.OUT_OF_RANGE, viewModel.uiState.value.fieldErrors[AssetFormField.LATITUDE])
        assertTrue(repository.updatedRequests.isEmpty())
    }

    @Test
    fun `server errors map to specific states`() = runTest {
        val viewModel = createViewModel()
        val cases = listOf(
            AppError.Api(403, "FORBIDDEN", "no") to CreateAssetError.Forbidden,
            AppError.Api(409, "ASSET_INACTIVE", "inactive") to CreateAssetError.AssetInactive,
            AppError.Api(404, "ASSET_NOT_FOUND", "gone") to CreateAssetError.NotFound
        )
        cases.forEach { (error, expected) ->
            repository.updateAssetResult = Outcome.Failure(error)
            viewModel.submit()
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(expected, viewModel.uiState.value.submitError)
            assertFalse(viewModel.uiState.value.saved)
        }
    }

    @Test
    fun `validation 400 and network errors are shown as generic errors and editing clears them`() = runTest {
        val viewModel = createViewModel()
        repository.updateAssetResult = Outcome.Failure(AppError.Api(400, "VALIDATION_ERROR", "bad"))
        viewModel.submit()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("bad", ((viewModel.uiState.value.submitError as CreateAssetError.Other).error as AppError.Api).detail)

        viewModel.onFieldChange(AssetFormField.NAME, "Pump 3")
        assertNull(viewModel.uiState.value.submitError)

        repository.updateAssetResult = Outcome.Failure(AppError.Network())
        viewModel.submit()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue((viewModel.uiState.value.submitError as CreateAssetError.Other).error is AppError.Network)
        assertEquals("Pump 3", viewModel.uiState.value.name)
    }

    @Test
    fun `unknown criticality falls back to the default so the edit is not blocked`() = runTest {
        repository.getAssetResult = Outcome.Success(existing.copy(criticality = Criticality.UNKNOWN))
        val viewModel = createViewModel()

        assertEquals(Criticality.MEDIUM, viewModel.uiState.value.criticality)
    }
}
