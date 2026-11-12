package pe.edu.upc.predictivemaintain.app.notifications.domain.repository

import pe.edu.upc.predictivemaintain.app.core.error.Outcome

interface DeviceTokenRepository {
    /** Registers [token] as a push destination of the authenticated user's device. */
    suspend fun register(token: String): Outcome<Unit>
}
