package pe.edu.upc.predictivemaintain.app.notifications.infrastructure.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.notifications.domain.service.PushTokenProvider
import javax.inject.Inject
import kotlin.coroutines.resume

class FirebasePushTokenProvider @Inject constructor() : PushTokenProvider {

    override suspend fun currentToken(): Outcome<String> {
        Log.d(TAG, "Requesting current FCM token")
        return try {
            suspendCancellableCoroutine { continuation ->
                FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token ->
                        Log.d(TAG, "FCM token obtained (${token.take(8)}…, length=${token.length})")
                        if (continuation.isActive) continuation.resume(Outcome.Success(token))
                    }
                    .addOnFailureListener { error ->
                        Log.d(TAG, "FCM token request FAILED: $error")
                        if (continuation.isActive) continuation.resume(Outcome.Failure(AppError.Unknown(error)))
                    }
            }
        } catch (e: IllegalStateException) {
            // FirebaseApp is not initialized: google-services.json / plugin missing or wrong.
            Log.d(TAG, "Firebase is not initialized: $e")
            Outcome.Failure(AppError.Unknown(e))
        }
    }

    private companion object {
        const val TAG = "FCM"
    }
}
