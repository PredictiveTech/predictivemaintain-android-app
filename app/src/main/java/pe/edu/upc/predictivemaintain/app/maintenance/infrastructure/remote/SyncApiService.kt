package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SyncApiService {

    @POST("sync/work-orders")
    suspend fun synchronizeWorkOrders(
        @Body request: SyncRequestDto
    ): Response<SyncResponseDto>
}
