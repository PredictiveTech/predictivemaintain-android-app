package pe.edu.upc.predictivemaintain.app.notifications.domain.service

import pe.edu.upc.predictivemaintain.app.core.error.Outcome

interface PushTokenProvider {
    /** Asks the push service (FCM) for the current registration token. */
    suspend fun currentToken(): Outcome<String>
}
