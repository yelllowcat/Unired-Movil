package com.unired.ui.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unired.data.model.Notification
import com.unired.data.repository.NotificationRepository
import com.unired.data.websocket.WebSocketManager
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val repository: NotificationRepository = NotificationRepository()
) : ViewModel() {

    var notifications by mutableStateOf<List<Notification>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var isPageLoading by mutableStateOf(false)
        private set

    var hasMore by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadNotifications()
        collectWebSocketNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            hasMore = true
            try {
                val initialNotifications = repository.getNotifications(limit = 20)
                notifications = initialNotifications
                hasMore = initialNotifications.size >= 20
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al cargar las notificaciones"
            } finally {
                isLoading = false
            }
        }
    }

    fun refreshNotifications() {
        viewModelScope.launch {
            isRefreshing = true
            try {
                val freshNotifications = repository.getNotifications(limit = 20)
                notifications = freshNotifications
                hasMore = freshNotifications.size >= 20
            } catch (e: Exception) {
                // Keep existing list on pull-to-refresh failure
            } finally {
                isRefreshing = false
            }
        }
    }

    fun loadNextPage() {
        if (isPageLoading || !hasMore || notifications.isEmpty()) return

        viewModelScope.launch {
            isPageLoading = true
            try {
                val lastId = notifications.lastOrNull()?.notificationId
                val newPage = repository.getNotifications(limit = 20, cursor = lastId)
                if (newPage.isEmpty()) {
                    hasMore = false
                } else {
                    val existingIds = notifications.map { it.notificationId }.toSet()
                    val filteredNewPage = newPage.filter { it.notificationId !in existingIds }
                    
                    if (filteredNewPage.isEmpty()) {
                        hasMore = false
                    } else {
                        notifications = notifications + filteredNewPage
                        if (newPage.size < 20) {
                            hasMore = false
                        }
                    }
                }
            } catch (e: Exception) {
                // Silently ignore page loading errors to avoid disrupting user experience
            } finally {
                isPageLoading = false
            }
        }
    }

    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            // Optimistic update — update UI immediately
            notifications = notifications.map { n ->
                if (n.notificationId == notificationId) n.copy(isRead = true) else n
            }
            try {
                repository.markAsRead(notificationId)
            } catch (_: Exception) {
                // Revert on failure
                notifications = notifications.map { n ->
                    if (n.notificationId == notificationId) n.copy(isRead = false) else n
                }
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            // Optimistic update
            notifications = notifications.map { it.copy(isRead = true) }
            try {
                repository.markAllAsRead()
            } catch (_: Exception) {
                // Best-effort; don't revert since the user already saw the feedback
            }
        }
    }

    /**
     * Collect new real-time notifications from the WebSocket and prepend them to the list
     * so the screen stays up-to-date without a full refresh.
     */
    private fun collectWebSocketNotifications() {
        viewModelScope.launch {
            WebSocketManager.incomingNotifications.collect { notification ->
                val alreadyPresent = notifications.any { it.notificationId == notification.notificationId }
                if (!alreadyPresent) {
                    notifications = listOf(notification) + notifications
                }
            }
        }
    }
}
