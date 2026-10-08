package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WorkOrderApiService {

    @POST("work-orders")
    suspend fun createWorkOrder(
        @Body request: CreateWorkOrderRequestDto
    ): Response<WorkOrderDto>

    @PATCH("work-orders/{id}/assign")
    suspend fun assignWorkOrder(
        @Path("id") id: String,
        @Body request: AssignWorkOrderRequestDto
    ): Response<WorkOrderDto>

    @POST("work-orders/{id}/start")
    suspend fun startWorkOrder(
        @Path("id") id: String,
        @Body request: StartWorkOrderRequestDto
    ): Response<WorkOrderDto>

    @PATCH("work-orders/{id}/status")
    suspend fun completeWorkOrder(
        @Path("id") id: String,
        @Body request: CompleteWorkOrderRequestDto
    ): Response<WorkOrderDto>

    @GET("work-orders")
    suspend fun getWorkOrders(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<WorkOrderPageDto>

    @GET("work-orders/{id}")
    suspend fun getWorkOrder(
        @Path("id") id: String
    ): Response<WorkOrderDto>
}
