package pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): Response<LoginResponseDto>

    @GET("users/me")
    suspend fun getMe(): Response<UserDto>
}
