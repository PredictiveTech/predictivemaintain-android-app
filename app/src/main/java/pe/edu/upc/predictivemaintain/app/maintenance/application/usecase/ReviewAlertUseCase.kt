package pe.edu.upc.predictivemaintain.app.maintenance.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Alert
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.AlertRepository
import javax.inject.Inject

class ReviewAlertUseCase @Inject constructor(
    private val alertRepository: AlertRepository
) {
    suspend operator fun invoke(
        id: String,
        status: String,
        reason: String?,
        expectedVersion: Long
    ): Outcome<Alert> {
        if (status.uppercase() == "DISCARDED") {
            if (reason.isNullOrBlank()) {
                return Outcome.Failure(
                    AppError.Api(
                        httpStatus = 400,
                        code = "VALIDATION_ERROR",
                        detail = "Discard reason cannot be blank",
                        fieldErrors = mapOf("reason" to "Discard reason cannot be blank")
                    )
                )
            }
            if (reason.length > 500) {
                return Outcome.Failure(
                    AppError.Api(
                        httpStatus = 400,
                        code = "VALIDATION_ERROR",
                        detail = "Discard reason must be at most 500 characters",
                        fieldErrors = mapOf("reason" to "Discard reason must be at most 500 characters")
                    )
                )
            }
        }
        return alertRepository.reviewAlert(id, status, reason, expectedVersion)
    }
}
