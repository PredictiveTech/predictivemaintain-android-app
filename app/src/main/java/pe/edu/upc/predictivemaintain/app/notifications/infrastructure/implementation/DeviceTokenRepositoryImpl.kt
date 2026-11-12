package pe.edu.upc.predictivemaintain.app.notifications.infrastructure.implementation

import android.util.Log
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.network.ErrorParser
import pe.edu.upc.predictivemaintain.app.core.network.safeApiCall
import pe.edu.upc.predictivemaintain.app.notifications.domain.repository.DeviceTokenRepository
import pe.edu.upc.predictivemaintain.app.notifications.infrastructure.remote.NotificationsApiService
import pe.edu.upc.predictivemaintain.app.notifications.infrastructure.remote.RegisterDeviceTokenRequestDto
import javax.inject.Inject

class DeviceTokenRepositoryImpl @Inject constructor(
    private val apiService: NotificationsApiService,
    private val errorParser: ErrorParser
) : DeviceTokenRepository {

    override suspend fun register(token: String): Outcome<Unit> {
        Log.d(TAG, "Sending FCM token to backend (${token.take(8)}…, length=${token.length})")
        val result = safeApiCall(errorParser) {
            apiService.registerDeviceToken(RegisterDeviceTokenRequestDto(token = token, platform = "ANDROID"))
        }
        when (result) {
            is Outcome.Success -> Log.d(TAG, "FCM token registered in backend: OK")
            is Outcome.Failure -> Log.d(TAG, "FCM token registration FAILED: ${result.error}")
        }
        return result
    }

    private companion object {
        const val TAG = "FCM"
    }
}
