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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.subscription.application.usecase.ChangePlanUseCase
import pe.edu.upc.predictivemaintain.app.subscription.application.usecase.GetSubscriptionUseCase
import pe.edu.upc.predictivemaintain.app.subscription.application.usecase.ListPlansUseCase
import pe.edu.upc.predictivemaintain.app.subscription.fakes.FakeSubscriptionRepository
import pe.edu.upc.predictivemaintain.app.subscription.fakes.basicPlan
import pe.edu.upc.predictivemaintain.app.subscription.fakes.proPlan
import pe.edu.upc.predictivemaintain.app.subscription.fakes.subscription
import pe.edu.upc.predictivemaintain.app.subscription.fakes.tinyPlan
import pe.edu.upc.predictivemaintain.app.subscription.presentation.state.PlanChangeFailure

@OptIn(ExperimentalCoroutinesApi::class)
class ChangePlanViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeSubscriptionRepository
    private val conflict = AppError.Api(409, "PLAN_TOO_SMALL", "Too many assets")

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeSubscriptionRepository() // Pro plan, 12 assets in use
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = ChangePlanViewModel(
        GetSubscriptionUseCase(repository), ListPlansUseCase(repository), ChangePlanUseCase(repository)
    ).also { dispatcher.scheduler.advanceUntilIdle() }

    private fun ChangePlanViewModel.confirm(plan: pe.edu.upc.predictivemaintain.app.subscription.domain.entity.SubscriptionPlan) {
        onPlanSelected(plan)
        confirmChange()
        dispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `lists the plans smallest first and knows the current one`() = runTest {
        val state = viewModel().uiState.value

        assertEquals(listOf("Starter", "Basic", "Pro"), state.plans.map { it.name })
        assertEquals(proPlan.id, state.currentPlanId)
        assertFalse(state.isLoading)
    }

    @Test
    fun `a load failure is reported and can be retried`() = runTest {
        repository.plansResult = Outcome.Failure(AppError.Network())
        val vm = viewModel()
        assertEquals("Network error. Please check your connection.", vm.uiState.value.loadError)
        assertTrue(vm.uiState.value.plans.isEmpty())

        repository.plansResult = Outcome.Success(listOf(proPlan, basicPlan))
        vm.load()
        dispatcher.scheduler.advanceUntilIdle()

        assertNull(vm.uiState.value.loadError)
        assertEquals(2, vm.uiState.value.plans.size)
    }

    @Test
    fun `tapping the current plan asks for nothing`() = runTest {
        val vm = viewModel()

        vm.onPlanSelected(proPlan)

        assertNull(vm.uiState.value.planToConfirm)
    }

    @Test
    fun `a tapped plan waits for confirmation and nothing is sent yet`() = runTest {
        val vm = viewModel()

        vm.onPlanSelected(basicPlan)

        assertEquals(basicPlan, vm.uiState.value.planToConfirm)
        assertTrue(repository.changeCalls.isEmpty())

        vm.dismissConfirmation()
        assertNull(vm.uiState.value.planToConfirm)
        assertTrue(repository.changeCalls.isEmpty())
    }

    @Test
    fun `confirming sends the plan id and exposes the new subscription`() = runTest {
        val upgraded = subscription(plan = basicPlan, used = 8)
        repository.changeResult = Outcome.Success(upgraded)
        val vm = viewModel()

        vm.confirm(basicPlan)

        assertEquals(listOf("p-basic"), repository.changeCalls)
        assertEquals(upgraded, vm.uiState.value.subscription)
        assertEquals(basicPlan.id, vm.uiState.value.currentPlanId)
        assertTrue(vm.uiState.value.changed)
        assertFalse(vm.uiState.value.isChanging)
        assertNull(vm.uiState.value.failure)
    }

    @Test
    fun `a 409 names how many assets must be deactivated using the fresh count`() = runTest {
        repository.changeResult = Outcome.Failure(conflict)
        val vm = viewModel() // 12 assets in use at load time
        repository.subscriptionResult = Outcome.Success(subscription(used = 14)) // two more registered since

        vm.confirm(tinyPlan) // limit 3

        val failure = vm.uiState.value.failure as PlanChangeFailure.PlanTooSmall
        assertEquals(tinyPlan, failure.plan)
        assertEquals(14L, failure.assetsInUse)
        assertEquals(11, failure.assetsToDeactivate)
        assertFalse(vm.uiState.value.changed)
        assertEquals(14L, vm.uiState.value.subscription!!.capacity.used)
    }

    @Test
    fun `a 409 is still explained when the fresh read fails`() = runTest {
        repository.changeResult = Outcome.Failure(conflict)
        val vm = viewModel()
        repository.subscriptionResult = Outcome.Failure(AppError.Network())

        vm.confirm(basicPlan) // limit 10, 12 in use

        val failure = vm.uiState.value.failure as PlanChangeFailure.PlanTooSmall
        assertEquals(2, failure.assetsToDeactivate)
    }

    @Test
    fun `a 409 that is not about capacity shows the server message`() = runTest {
        repository.changeResult = Outcome.Failure(AppError.Api(409, "SUBSCRIPTION_CANCELLED", "Subscription is cancelled"))
        val vm = viewModel()

        vm.confirm(proPlan.copy(id = "p-bigger", assetLimit = 500)) // fits easily

        assertEquals(PlanChangeFailure.Other("Subscription is cancelled"), vm.uiState.value.failure)
    }

    @Test
    fun `other failures show their own message and keep the screen usable`() = runTest {
        repository.changeResult = Outcome.Failure(AppError.Network())
        val vm = viewModel()

        vm.confirm(basicPlan)

        assertEquals(
            PlanChangeFailure.Other("Network error. Please check your connection."),
            vm.uiState.value.failure
        )
        assertFalse(vm.uiState.value.isChanging)
        assertFalse(vm.uiState.value.changed)
        assertEquals(3, vm.uiState.value.plans.size)

        vm.dismissFailure()
        assertNull(vm.uiState.value.failure)
    }

    @Test
    fun `selecting a plan clears the previous failure`() = runTest {
        repository.changeResult = Outcome.Failure(conflict)
        val vm = viewModel()
        vm.confirm(tinyPlan)

        vm.onPlanSelected(basicPlan)

        assertNull(vm.uiState.value.failure)
    }

    @Test
    fun `confirm without a selected plan does nothing`() = runTest {
        val vm = viewModel()

        vm.confirmChange()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(repository.changeCalls.isEmpty())
    }
}
