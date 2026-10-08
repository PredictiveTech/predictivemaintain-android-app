package pe.edu.upc.predictivemaintain.app.iam.domain.repository

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.AuthSession
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.UserProfile
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Email
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Password

interface AuthRepository {
    suspend fun login(email: Email, password: Password): Outcome<AuthSession>
    suspend fun fetchProfile(): Outcome<UserProfile>
}
