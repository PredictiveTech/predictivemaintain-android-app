package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

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

    @Multipart
    @POST("work-orders/{id}/attachments")
    suspend fun uploadAttachment(
        @Path("id") id: String,
        @Part file: MultipartBody.Part
    ): Response<Unit>

    @GET("work-orders/{id}/attachments")
    suspend fun getAttachments(
        @Path("id") id: String
    ): Response<List<EvidenceDto>>

    /** Raw image bytes. Requires the Bearer token (there is no public URL). The UI loads them through Coil. */
    @Streaming
    @GET("work-orders/{id}/attachments/{attachmentId}")
    suspend fun downloadAttachment(
        @Path("id") id: String,
        @Path("attachmentId") attachmentId: String
    ): Response<ResponseBody>

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
