package pe.edu.upc.predictivemaintain.app.maintenance.application.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.fakes.FakeAlertRepository

class ReviewAlertUseCaseTest {

    private lateinit var fakeRepository: FakeAlertRepository
    private lateinit var reviewAlertUseCase: ReviewAlertUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeAlertRepository()
        reviewAlertUseCase = ReviewAlertUseCase(fakeRepository)
    }

    @Test
    fun `blank discard reason is rejected without calling repository`() = runTest {
        val result = reviewAlertUseCase("a-1", "DISCARDED", "", 1L)

        assertTrue(result is Outcome.Failure)
        val error = (result as Outcome.Failure).error
        assertTrue(error is AppError.Api)
        assertEquals("VALIDATION_ERROR", (error as AppError.Api).code)
    }
}
