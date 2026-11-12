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
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.CreateAssetUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Asset
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetCode
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetStatus
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.Criticality
import pe.edu.upc.predictivemaintain.app.maintenance.fakes.FakeAssetRepository
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.AssetFormField
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.CreateAssetError
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.FieldError

@OptIn(ExperimentalCoroutinesApi::class)
class CreateAssetViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeAssetRepository
    private lateinit var viewModel: CreateAssetViewModel

    private val createdAsset = Asset(
        id = AssetId("a-1"), code = AssetCode("P-001"), name = "Pump", location = "Plant A",
        productionLine = null, assetType = "PUMP", criticality = Criticality.HIGH, active = true,
        status = AssetStatus.OPERATIONAL, sensorTypes = emptyList()
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeAssetRepository()
        viewModel = CreateAssetViewModel(CreateAssetUseCase(repository))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fillRequired() {
        viewModel.onFieldChange(AssetFormField.CODE, " P-001 ")
        viewModel.onFieldChange(AssetFormField.NAME, "Pump")
        viewModel.onFieldChange(AssetFormField.LOCATION, "Plant A")
        viewModel.onFieldChange(AssetFormField.ASSET_TYPE, "PUMP")
        viewModel.onCriticalityChange(Criticality.HIGH)
    }

    private fun submitAndWait() {
        viewModel.submit()
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `empty form shows required errors and does not call the server`() = runTest {
        submitAndWait()

        val errors = viewModel.uiState.value.fieldErrors
        assertEquals(FieldError.REQUIRED, errors[AssetFormField.CODE])
        assertEquals(FieldError.REQUIRED, errors[AssetFormField.NAME])
        assertEquals(FieldError.REQUIRED, errors[AssetFormField.LOCATION])
        assertEquals(FieldError.REQUIRED, errors[AssetFormField.ASSET_TYPE])
        assertTrue(repository.createdRequests.isEmpty())
    }

    @Test
    fun `blank and too long values are rejected`() = runTest {
        fillRequired()
        viewModel.onFieldChange(AssetFormField.NAME, "   ")
        viewModel.onFieldChange(AssetFormField.CODE, "x".repeat(51))
        submitAndWait()

        val errors = viewModel.uiState.value.fieldErrors
        assertEquals(FieldError.REQUIRED, errors[AssetFormField.NAME])
        assertEquals(FieldError.TOO_LONG, errors[AssetFormField.CODE])
        assertTrue(repository.createdRequests.isEmpty())
    }

    @Test
    fun `editing a field clears its error`() = runTest {
        submitAndWait()
        viewModel.onFieldChange(AssetFormField.CODE, "P")

        assertFalse(AssetFormField.CODE in viewModel.uiState.value.fieldErrors)
        assertTrue(AssetFormField.NAME in viewModel.uiState.value.fieldErrors)
    }

    @Test
    fun `coordinates must come in pairs and be in range`() = runTest {
        fillRequired()
        viewModel.onFieldChange(AssetFormField.LATITUDE, "-12.04")
        submitAndWait()
        assertEquals(FieldError.COORDINATES_INCOMPLETE, viewModel.uiState.value.fieldErrors[AssetFormField.LONGITUDE])

        viewModel.onFieldChange(AssetFormField.LONGITUDE, "181")
        submitAndWait()
        assertEquals(FieldError.OUT_OF_RANGE, viewModel.uiState.value.fieldErrors[AssetFormField.LONGITUDE])

        viewModel.onFieldChange(AssetFormField.LONGITUDE, "abc")
        submitAndWait()
        assertEquals(FieldError.INVALID_NUMBER, viewModel.uiState.value.fieldErrors[AssetFormField.LONGITUDE])

        viewModel.onFieldChange(AssetFormField.LATITUDE, "NaN")
        viewModel.onFieldChange(AssetFormField.LONGITUDE, "-77.03")
        submitAndWait()
        assertEquals(FieldError.INVALID_NUMBER, viewModel.uiState.value.fieldErrors[AssetFormField.LATITUDE])
        assertTrue(repository.createdRequests.isEmpty())
    }

    @Test
    fun `valid submit sends trimmed data and marks saved`() = runTest {
        repository.createAssetResult = Outcome.Success(createdAsset)
        fillRequired()
        viewModel.onFieldChange(AssetFormField.PRODUCTION_LINE, "  ")
        viewModel.onFieldChange(AssetFormField.LATITUDE, "-12,5")
        viewModel.onFieldChange(AssetFormField.LONGITUDE, "-77.03")
        submitAndWait()

        val sent = repository.createdRequests.single()
        assertEquals("P-001", sent.code)
        assertEquals(Criticality.HIGH, sent.criticality)
        assertNull(sent.productionLine)
        assertEquals(-12.5, sent.latitude!!, 0.0)
        assertEquals(-77.03, sent.longitude!!, 0.0)
        val state = viewModel.uiState.value
        assertTrue(state.saved)
        assertFalse(state.isSubmitting)
        assertNull(state.submitError)
    }

    @Test
    fun `optional fields can be left empty`() = runTest {
        repository.createAssetResult = Outcome.Success(createdAsset)
        fillRequired()
        submitAndWait()

        val sent = repository.createdRequests.single()
        assertNull(sent.productionLine)
        assertNull(sent.latitude)
        assertNull(sent.longitude)
    }

    @Test
    fun `409 duplicate code marks the code field`() = runTest {
        repository.createAssetResult = Outcome.Failure(AppError.Api(409, "ASSET_CODE_ALREADY_EXISTS", "exists"))
        fillRequired()
        submitAndWait()

        val state = viewModel.uiState.value
        assertEquals(CreateAssetError.DuplicateCode, state.submitError)
        assertEquals(FieldError.DUPLICATE_CODE, state.fieldErrors[AssetFormField.CODE])
        assertFalse(state.saved)
        assertFalse(state.isSubmitting)
    }

    @Test
    fun `403 capacity exceeded is told apart from a plain 403`() = runTest {
        repository.createAssetResult = Outcome.Failure(AppError.Api(403, "CAPACITY_EXCEEDED", "full"))
        fillRequired()
        submitAndWait()
        assertEquals(CreateAssetError.CapacityExceeded, viewModel.uiState.value.submitError)

        repository.createAssetResult = Outcome.Failure(AppError.Api(403, "FORBIDDEN", "no"))
        submitAndWait()
        assertEquals(CreateAssetError.Forbidden, viewModel.uiState.value.submitError)
    }

    @Test
    fun `network failure keeps the form and exposes the error`() = runTest {
        repository.createAssetResult = Outcome.Failure(AppError.Network())
        fillRequired()
        submitAndWait()

        val state = viewModel.uiState.value
        assertTrue((state.submitError as CreateAssetError.Other).error is AppError.Network)
        assertEquals("Pump", state.name)
        assertFalse(state.saved)
    }

    @Test
    fun `editing after a server error clears it`() = runTest {
        repository.createAssetResult = Outcome.Failure(AppError.Network())
        fillRequired()
        submitAndWait()
        viewModel.onFieldChange(AssetFormField.NAME, "Pump 2")

        assertNull(viewModel.uiState.value.submitError)
    }
}
