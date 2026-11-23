package pe.edu.upc.predictivemaintain.app.subscription.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface SubscriptionApiService {
    @GET("subscription")
    suspend fun getSubscription(): Response<SubscriptionDto>

    @GET("plans")
    suspend fun getPlans(): Response<List<PlanDto>>

    @PUT("subscription/plan")
    suspend fun changePlan(@Body request: ChangePlanRequestDto): Response<SubscriptionDto>
}
