package pe.edu.upc.predictivemaintain.app.notifications.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface NotificationsApiService {

    /**
     * ASSUMED contract (not confirmed with the backend; this repo has no server code):
     * POST {BASE_URL}notifications/device-tokens  body { "token": "...", "platform": "ANDROID" }, Bearer auth,
     * 2xx on success (idempotent upsert by token). Adjust here if the real route/body differs.
     */
    @POST("notifications/device-tokens")
    suspend fun registerDeviceToken(
        @Body request: RegisterDeviceTokenRequestDto
    ): Response<Unit>
}
