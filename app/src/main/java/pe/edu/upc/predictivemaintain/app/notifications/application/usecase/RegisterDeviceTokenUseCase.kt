package pe.edu.upc.predictivemaintain.app.notifications.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.SessionRepository
import pe.edu.upc.predictivemaintain.app.notifications.domain.repository.DeviceTokenRepository
import pe.edu.upc.predictivemaintain.app.notifications.domain.service.PushTokenProvider
import javax.inject.Inject

/**
 * Sends the FCM token to the backend. With no [token] it asks FCM for the current one (after login / app start);
 * [PredictiveMaintainFirebaseMessagingService.onNewToken] passes the rotated token explicitly.
 * Needs a session: without one it fails with [AppError.SessionExpired] and the caller just waits for the next login.
 */
class RegisterDeviceTokenUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val pushTokenProvider: PushTokenProvider,
    private val deviceTokenRepository: DeviceTokenRepository
) {
    suspend operator fun invoke(token: String? = null): Outcome<Unit> {
        if (sessionRepository.currentSession() == null) return Outcome.Failure(AppError.SessionExpired)
        val resolved = token?.takeIf { it.isNotBlank() }
            ?: when (val current = pushTokenProvider.currentToken()) {
                is Outcome.Success -> current.data
                is Outcome.Failure -> return current
            }
        return deviceTokenRepository.register(resolved)
    }
}
