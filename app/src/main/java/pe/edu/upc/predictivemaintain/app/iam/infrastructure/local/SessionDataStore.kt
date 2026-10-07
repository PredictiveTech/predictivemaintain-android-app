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
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.TenantId
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.UserId
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.mapper.AuthMapper
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
        // 1. Filled when the stored session is first read from disk
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.data.collect { prefs ->
                val token = prefs[KEY_ACCESS_TOKEN]
                cachedAccessToken.set(token)
            }
        }
    }

    override fun getAccessToken(): String? {
        return cachedAccessToken.get()
    }

    fun observeSession(): Flow<AuthSession?> {
        return context.dataStore.data.map { prefs ->
            val token = prefs[KEY_ACCESS_TOKEN]
            cachedAccessToken.set(token) // also update cache on read

            if (token == null) return@map null
            val expiresAtStr = prefs[KEY_EXPIRES_AT] ?: return@map clearAndReturnNull()
            val userId = prefs[KEY_USER_ID] ?: return@map clearAndReturnNull()
            val tenantId = prefs[KEY_TENANT_ID] ?: return@map clearAndReturnNull()
            val rolesStr = prefs[KEY_ROLES] ?: return@map clearAndReturnNull()

            try {
                // 2. Roles read from disk converted with AuthMapper.parseRoles inside try/catch for IllegalArgumentException only
                val roleList = rolesStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val roles = AuthMapper.parseRoles(roleList)
                val expiresAt = Instant.parse(expiresAtStr)

                AuthSession(
                    userId = UserId(userId),
                    tenantId = TenantId(tenantId),
                    roles = roles,
                    accessToken = AccessToken(token),
                    expiresAt = expiresAt
                )
            } catch (_: IllegalArgumentException) {
                // Corrupt data: treat as no session, clear storage and cache, never throw
                clearAndReturnNull()
            } catch (_: DateTimeParseException) {
                // Unreadable expiry: treat as no session, clear storage and cache, never throw
                clearAndReturnNull()
            }
        }
    }

    private suspend fun clearAndReturnNull(): AuthSession? {
        // 3. Set to null when session is cleared / corrupt
        cachedAccessToken.set(null)
        try {
            context.dataStore.edit { it.clear() }
        } catch (_: Exception) {}
        return null
    }

    suspend fun currentSession(): AuthSession? {
        return observeSession().first()
    }

    suspend fun saveSession(session: AuthSession) {
        // 2. Filled when a session is saved
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
        // 3. Set to null when the session is cleared
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
