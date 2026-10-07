package pe.edu.upc.predictivemaintain.app.iam.infrastructure.implementation

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.network.ErrorParser
import pe.edu.upc.predictivemaintain.app.core.network.safeApiCall
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.AuthSession
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.UserProfile
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.AuthRepository
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Email
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Password
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.mapper.AuthMapper
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote.AuthApiService
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote.LoginRequestDto
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val errorParser: ErrorParser
) : AuthRepository {

    override suspend fun login(email: Email, password: Password): Outcome<AuthSession> {
        val requestDto = LoginRequestDto(
            email = email.value,
            password = password.value
        )
        return when (val apiResult = safeApiCall(errorParser) { authApiService.login(requestDto) }) {
            is Outcome.Success -> AuthMapper.toDomain(apiResult.data)
            is Outcome.Failure -> apiResult
        }
    }

    override suspend fun fetchProfile(): Outcome<UserProfile> {
        return when (val apiResult = safeApiCall(errorParser) { authApiService.getMe() }) {
            is Outcome.Success -> AuthMapper.toDomain(apiResult.data)
            is Outcome.Failure -> apiResult
        }
    }
}
