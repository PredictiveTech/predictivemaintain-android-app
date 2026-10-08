package pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface UserDirectoryApiService {

    @GET("users")
    suspend fun getUsers(
        @Query("role") role: String = "TECHNICIAN",
        @Query("active") active: Boolean = true,
        @Query("size") size: Int = 100
    ): Response<UserPageDto>
}
