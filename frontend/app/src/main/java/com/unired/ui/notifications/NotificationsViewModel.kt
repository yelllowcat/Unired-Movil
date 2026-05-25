package com.unired.ui.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unired.data.model.Notification
import com.unired.data.repository.NotificationRepository
import kotlinx.coroutines.launch

sealed interface NotificationsUiState {
    object Loading : NotificationsUiState
    data class Success(val notifications: List<Notification>) : NotificationsUiState
    data class Error(val message: String) : NotificationsUiState
}

class NotificationsViewModel(
    private val notificationRepository: NotificationRepository = NotificationRepository()
) : ViewModel() {

    var uiState by mutableStateOf<NotificationsUiState>(NotificationsUiState.Loading)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            uiState = NotificationsUiState.Loading
            try {
                val notifications = notificationRepository.getNotifications()
                uiState = NotificationsUiState.Success(notifications)
            } catch (e: Exception) {
                uiState = NotificationsUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun refreshNotifications() {
        viewModelScope.launch {
            isRefreshing = true
            try {
                val notifications = notificationRepository.getNotifications()
                uiState = NotificationsUiState.Success(notifications)
            } catch (e: Exception) {
                val currentState = uiState
                if (currentState !is NotificationsUiState.Success) {
                    uiState = NotificationsUiState.Error(e.message ?: "Error al actualizar notificaciones")
                }
            } finally {
                isRefreshing = false
            }
        }
    }

    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            val currentState = uiState
            if (currentState is NotificationsUiState.Success) {
                val updatedList = currentState.notifications.map {
                    if (it.notificationId == notificationId) it.copy(isRead = true) else it
                }
                uiState = NotificationsUiState.Success(updatedList)
            }
            try {
                notificationRepository.markAsRead(notificationId)
            } catch (_: Exception) {
                refreshNotifications()
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            val currentState = uiState
            if (currentState is NotificationsUiState.Success) {
                val updatedList = currentState.notifications.map { it.copy(isRead = true) }
                uiState = NotificationsUiState.Success(updatedList)
            }
            try {
                notificationRepository.markAllAsRead()
            } catch (_: Exception) {
                refreshNotifications()
            }
        }
    }
}
