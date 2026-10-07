package pe.edu.upc.predictivemaintain.app.iam.infrastructure.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import pe.edu.upc.predictivemaintain.app.core.network.AccessTokenProvider
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.AuthSession
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.AccessToken
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.TenantId
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.UserId
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_session_prefs")

data class SessionLocalModel(
    val userId: String,
    val tenantId: String,
    val roles: List<String>,
    val accessToken: String,
    val expiresAt: String
) {
    override fun toString(): String {
        return "SessionLocalModel(userId=$userId, tenantId=$tenantId, roles=$roles, accessToken=***, expiresAt=$expiresAt)"
    }
}

@Singleton
class SessionDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) : AccessTokenProvider {

    private val cachedAccessToken = AtomicReference<String?>(null)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.data.collect { prefs ->
                cachedAccessToken.set(prefs[KEY_ACCESS_TOKEN])
            }
        }
    }

    override fun getAccessToken(): String? {
        return cachedAccessToken.get()
    }

    fun observeSession(): Flow<AuthSession?> {
        return context.dataStore.data.map { prefs ->
            val token = prefs[KEY_ACCESS_TOKEN] ?: return@map null
            val expiresAtStr = prefs[KEY_EXPIRES_AT] ?: return@map null
            val userId = prefs[KEY_USER_ID] ?: return@map null
            val tenantId = prefs[KEY_TENANT_ID] ?: return@map null
            val rolesStr = prefs[KEY_ROLES] ?: return@map null

            if (rolesStr.isBlank()) return@map null

            try {
                val roles = rolesStr.split(",").map { roleName -> Role.fromString(roleName) }
                if (roles.isEmpty()) return@map null

                AuthSession(
                    userId = UserId(userId),
                    tenantId = TenantId(tenantId),
                    roles = roles,
                    accessToken = AccessToken(token),
                    expiresAt = Instant.parse(expiresAtStr)
                )
            } catch (_: IllegalArgumentException) {
                null
            } catch (_: DateTimeParseException) {
                null
            }
        }
    }

    suspend fun currentSession(): AuthSession? {
        return observeSession().first()
    }

    suspend fun saveSession(session: AuthSession) {
        cachedAccessToken.set(session.accessToken.value)
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = session.accessToken.value
            prefs[KEY_EXPIRES_AT] = session.expiresAt.toString()
            prefs[KEY_USER_ID] = session.userId.value
            prefs[KEY_TENANT_ID] = session.tenantId.value
            prefs[KEY_ROLES] = session.roles.joinToString(",") { it.name }
        }
    }

    suspend fun clearSession() {
        cachedAccessToken.set(null)
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_EXPIRES_AT = stringPreferencesKey("expires_at")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_TENANT_ID = stringPreferencesKey("tenant_id")
        private val KEY_ROLES = stringPreferencesKey("roles")
    }
}
