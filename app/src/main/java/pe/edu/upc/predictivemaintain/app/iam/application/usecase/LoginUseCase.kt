package pe.edu.upc.predictivemaintain.app.iam.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.AuthSession
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.AuthRepository
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.SessionRepository
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Email
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Password
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(rawEmail: String, rawPassword: String): Outcome<AuthSession> {
        val fieldErrors = mutableMapOf<String, String>()

        if (rawEmail.isBlank()) {
            fieldErrors["email"] = "Email cannot be blank"
        } else if (!Email.isValid(rawEmail)) {
            fieldErrors["email"] = "Invalid email format"
        }

        if (rawPassword.isBlank()) {
            fieldErrors["password"] = "Password cannot be blank"
        }

        if (fieldErrors.isNotEmpty()) {
            return Outcome.Failure(
                AppError.Api(
                    httpStatus = 400,
                    code = "VALIDATION_ERROR",
                    detail = "Invalid input fields",
                    fieldErrors = fieldErrors
                )
            )
        }

        val email = Email(rawEmail.trim())
        val password = Password(rawPassword)

        val result = authRepository.login(email, password)
        if (result is Outcome.Success) {
            sessionRepository.saveSession(result.data)
        }
        return result
    }
}
