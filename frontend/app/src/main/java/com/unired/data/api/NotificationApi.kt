package com.unired.data.api

import com.unired.data.model.Notification
import com.unired.data.model.response.UnreadCountResponse
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApi {
    @GET("notifications")
    suspend fun getNotifications(
        @Query("limit") limit: Int,
        @Query("cursor") cursor: Int? = null
    ): ApiResponse<List<Notification>>

    @GET("notifications/unread-count")
    suspend fun getUnreadCount(): ApiResponse<UnreadCountResponse>

    @PUT("notifications/mark-all-read")
    suspend fun markAllAsRead(): ApiResponse<Unit>

    @PUT("notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: Int): ApiResponse<Notification>
}
