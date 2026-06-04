package com.unired.data.repository

import com.unired.data.api.ApiClient
import com.unired.data.api.NotificationApi
import com.unired.data.model.Notification

class NotificationRepository(
    private val api: NotificationApi = ApiClient.retrofit.create(NotificationApi::class.java)
) {
    suspend fun getNotifications(limit: Int = 20, cursor: Int? = null): List<Notification> {
        return safeApiCall { api.getNotifications(limit, cursor) }
    }

    suspend fun getUnreadCount(): Int {
        val response = safeApiCall { api.getUnreadCount() }
        return response.unreadCount
    }

    suspend fun markAllAsRead() {
        safeApiCallUnit { api.markAllAsRead() }
    }

    suspend fun markAsRead(notificationId: Int): Notification {
        return safeApiCall { api.markAsRead(notificationId) }
    }
}
