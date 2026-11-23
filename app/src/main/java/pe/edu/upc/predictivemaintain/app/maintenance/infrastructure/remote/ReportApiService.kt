package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ReportApiService {
    @GET("reports/availability")
    suspend fun getAvailability(
        @Query("assetId") assetId: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): Response<AvailabilityReportDto>

    /** [assetIds] is a comma separated list (1 to 20 ids), as the spec describes. */
    @GET("reports/comparison")
    suspend fun compare(
        @Query("assetIds") assetIds: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): Response<List<ComparisonRowDto>>

    @Streaming
    @GET("reports/history/export")
    suspend fun exportHistory(
        @Query("assetIds") assetIds: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): Response<ResponseBody>
}
