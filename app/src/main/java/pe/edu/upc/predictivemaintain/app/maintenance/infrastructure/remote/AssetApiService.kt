package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AssetApiService {
    @GET("assets")
    suspend fun getAssets(
        @Query("status") status: String?,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<AssetPageDto>

    @GET("assets/{id}")
    suspend fun getAsset(
        @Path("id") id: String
    ): Response<AssetDetailDto>
}
