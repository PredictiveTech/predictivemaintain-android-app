package pe.edu.upc.predictivemaintain.app.subscription.presentation.viewmodel

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
import org.junit.Before
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.subscription.application.usecase.GetSubscriptionUseCase
import pe.edu.upc.predictivemaintain.app.subscription.fakes.FakeSubscriptionRepository
import pe.edu.upc.predictivemaintain.app.subscription.fakes.subscription

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeSubscriptionRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeSubscriptionRepository()
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = SubscriptionViewModel(GetSubscriptionUseCase(repository))
        .also { dispatcher.scheduler.advanceUntilIdle() }

    @Test
    fun `loads the subscription when it opens`() = runTest {
        val vm = viewModel()

        assertEquals(subscription(), vm.uiState.value.subscription)
        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `keeps the server flags so the screen can warn`() = runTest {
        repository.subscriptionResult = Outcome.Success(subscription(used = 46, nearLimit = true, expiresSoon = true, daysRemaining = 3))

        val s = viewModel().uiState.value.subscription!!

        assertEquals(true, s.capacity.nearLimit)
        assertEquals(true, s.expiry.expiresSoon)
        assertEquals(3L, s.expiry.daysRemaining)
    }

    @Test
    fun `a failure shows the server message`() = runTest {
        repository.subscriptionResult = Outcome.Failure(AppError.Api(403, "FORBIDDEN", "Managers only"))

        val vm = viewModel()

        assertEquals("Managers only", vm.uiState.value.errorMessage)
        assertNull(vm.uiState.value.subscription)
    }

    @Test
    fun `a network failure shows a connection message and retry recovers`() = runTest {
        repository.subscriptionResult = Outcome.Failure(AppError.Network())
        val vm = viewModel()
        assertEquals("Network error. Please check your connection.", vm.uiState.value.errorMessage)

        repository.subscriptionResult = Outcome.Success(subscription())
        vm.load()
        dispatcher.scheduler.advanceUntilIdle()

        assertNull(vm.uiState.value.errorMessage)
        assertEquals(subscription(), vm.uiState.value.subscription)
    }

    @Test
    fun `a failed refresh keeps the data already on screen`() = runTest {
        val vm = viewModel()

        repository.subscriptionResult = Outcome.Failure(AppError.Network())
        vm.load()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(subscription(), vm.uiState.value.subscription)
        assertEquals("Network error. Please check your connection.", vm.uiState.value.errorMessage)
    }
}
