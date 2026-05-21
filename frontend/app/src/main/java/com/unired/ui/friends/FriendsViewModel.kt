package com.unired.ui.friends

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unired.data.model.FriendRequest
import com.unired.data.model.dto.UserPreview
import com.unired.data.repository.FriendRepository
import com.unired.data.repository.UserRepository
import com.unired.util.SessionManager
import kotlinx.coroutines.launch

enum class FriendsTab {
    SOLICITUDES,
    ENVIAR_SOLICITUD,
    PENDIENTES
}

class FriendsViewModel(
    private val friendRepository: FriendRepository = FriendRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    var currentTab by mutableStateOf(FriendsTab.SOLICITUDES)
        private set

    var currentUserFullName by mutableStateOf("Usuario")
        private set

    var searchQuery by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var incomingRequests by mutableStateOf<List<FriendRequest>>(emptyList())
        private set

    var sentRequests by mutableStateOf<List<FriendRequest>>(emptyList())
        private set

    var searchResults by mutableStateOf<List<UserPreview>>(emptyList())
        private set

    init {
        loadUserProfile()
        loadTabContent(FriendsTab.SOLICITUDES)
    }

    fun loadUserProfile() {
        val currentUserId = SessionManager.getUserId()
        if (currentUserId != -1) {
            viewModelScope.launch {
                try {
                    val profile = userRepository.getProfile(currentUserId)
                    currentUserFullName = profile.fullName
                } catch (e: Exception) {
                    // Fail silently
                }
            }
        }
    }

    fun onTabChange(tab: FriendsTab) {
        currentTab = tab
        loadTabContent(tab)
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        if (currentTab == FriendsTab.ENVIAR_SOLICITUD) {
            performSearch()
        }
    }

    fun performSearch() {
        if (searchQuery.isBlank()) {
            searchResults = emptyList()
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val results = userRepository.searchUsers(searchQuery)
                val currentUserId = SessionManager.getUserId()
                // Filter out current user from search
                searchResults = results.filter { it.userId != currentUserId }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al buscar usuarios"
            } finally {
                isLoading = false
            }
        }
    }

    fun loadTabContent(tab: FriendsTab) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                when (tab) {
                    FriendsTab.SOLICITUDES -> {
                        incomingRequests = friendRepository.getPendingRequests()
                    }
                    FriendsTab.ENVIAR_SOLICITUD -> {
                        if (searchQuery.isNotBlank()) {
                            performSearch()
                        } else {
                            searchResults = emptyList()
                        }
                    }
                    FriendsTab.PENDIENTES -> {
                        sentRequests = friendRepository.getSentRequests()
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al cargar información"
            } finally {
                isLoading = false
            }
        }
    }

    fun sendRequest(receiverId: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                friendRepository.sendRequest(receiverId)
                // Refresh to show that the request was sent
                loadTabContent(currentTab)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al enviar la solicitud"
            } finally {
                isLoading = false
            }
        }
    }

    fun acceptRequest(requestId: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                friendRepository.respondToRequest(requestId, "accepted")
                loadTabContent(FriendsTab.SOLICITUDES)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al aceptar la solicitud"
            } finally {
                isLoading = false
            }
        }
    }

    fun rejectRequest(requestId: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                friendRepository.respondToRequest(requestId, "rejected")
                loadTabContent(FriendsTab.SOLICITUDES)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al rechazar la solicitud"
            } finally {
                isLoading = false
            }
        }
    }

    fun cancelRequest(requestId: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                friendRepository.cancelRequest(requestId)
                loadTabContent(FriendsTab.PENDIENTES)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al cancelar la solicitud"
            } finally {
                isLoading = false
            }
        }
    }
}
