package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface AlertApiService {
    @GET("alerts")
    suspend fun getAlerts(
        @Query("severity") severity: String?,
        @Query("status") status: String?,
        @Query("assetId") assetId: String?,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<AlertPageDto>

    @GET("alerts/{id}")
    suspend fun getAlert(
        @Path("id") id: String
    ): Response<AlertDto>

    @PATCH("alerts/{id}/status")
    suspend fun reviewAlert(
        @Path("id") id: String,
        @Body request: ReviewAlertRequestDto
    ): Response<AlertDto>
}
