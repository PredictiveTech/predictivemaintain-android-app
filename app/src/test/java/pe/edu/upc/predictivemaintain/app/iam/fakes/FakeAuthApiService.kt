package pe.edu.upc.predictivemaintain.app.iam.fakes

import pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote.AuthApiService
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote.LoginRequestDto
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote.LoginResponseDto
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote.UserDto
import retrofit2.Response

class FakeAuthApiService : AuthApiService {

    var loginResponse: Response<LoginResponseDto>? = null
    var getMeResponse: Response<UserDto>? = null

    var lastLoginRequest: LoginRequestDto? = null

    override suspend fun login(request: LoginRequestDto): Response<LoginResponseDto> {
        lastLoginRequest = request
        return loginResponse ?: Response.success(
            LoginResponseDto(
                accessToken = "fake-api-token",
                tokenType = "Bearer",
                expiresAt = "2026-03-01T15:00:00Z",
                userId = "u-fake",
                tenantId = "t-fake",
                roles = listOf("TECHNICIAN")
            )
        )
    }

    override suspend fun getMe(): Response<UserDto> {
        return getMeResponse ?: Response.success(
            UserDto(
                id = "u-fake",
                tenantId = "t-fake",
                email = "user@plant.com",
                displayName = "Fake User",
                active = true,
                roles = listOf("TECHNICIAN")
            )
        )
    }
}
