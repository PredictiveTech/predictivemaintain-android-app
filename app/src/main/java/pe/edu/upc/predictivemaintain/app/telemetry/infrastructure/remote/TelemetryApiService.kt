package pe.edu.upc.predictivemaintain.app.telemetry.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface TelemetryApiService {
    @GET("assets/{assetId}/sensors")
    suspend fun getSensors(
        @Path("assetId") assetId: String
    ): Response<List<SensorItemDto>>
}
