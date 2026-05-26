package com.unired.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unired.data.model.Post
import com.unired.data.model.User
import com.unired.data.repository.FriendRepository
import com.unired.data.repository.PostRepository
import com.unired.data.repository.UserRepository
import com.unired.util.SessionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Success(val user: User, val posts: List<Post>) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

class ProfileViewModel(
    private val userIdArg: String,
    private val userRepository: UserRepository = UserRepository(),
    private val postRepository: PostRepository = PostRepository(),
    private val friendRepository: FriendRepository = FriendRepository()
) : ViewModel() {

    var uiState by mutableStateOf<ProfileUiState>(ProfileUiState.Loading)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    val resolvedUserId: Int
        get() {
            return if (userIdArg == "me" || userIdArg.toIntOrNull() == null) {
                SessionManager.getUserId()
            } else {
                userIdArg.toInt()
            }
        }

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            uiState = ProfileUiState.Loading
            try {
                coroutineScope {
                    val id = resolvedUserId
                    if (id == -1) {
                        uiState = ProfileUiState.Error("Usuario no autenticado")
                        return@coroutineScope
                    }
                    
                    val profileDeferred = async { userRepository.getProfile(id) }
                    val postsDeferred = async { userRepository.getUserPosts(id) }
                    
                    val user = profileDeferred.await()
                    val posts = postsDeferred.await()
                    
                    uiState = ProfileUiState.Success(user, posts)
                }
            } catch (e: Exception) {
                uiState = ProfileUiState.Error(e.message ?: "Error al cargar el perfil")
            }
        }
    }

    fun refreshProfile() {
        viewModelScope.launch {
            isRefreshing = true
            try {
                coroutineScope {
                    val id = resolvedUserId
                    if (id != -1) {
                        val profileDeferred = async { userRepository.getProfile(id) }
                        val postsDeferred = async { userRepository.getUserPosts(id) }
                        
                        val user = profileDeferred.await()
                        val posts = postsDeferred.await()
                        
                        uiState = ProfileUiState.Success(user, posts)
                    }
                }
            } catch (e: Exception) {
                val currentState = uiState
                if (currentState !is ProfileUiState.Success) {
                    uiState = ProfileUiState.Error(e.message ?: "Error al actualizar perfil")
                }
            } finally {
                isRefreshing = false
            }
        }
    }

    fun togglePostLike(postId: Int) {
        viewModelScope.launch {
            val currentState = uiState
            if (currentState is ProfileUiState.Success) {
                val updatedPosts = currentState.posts.map { post ->
                    if (post.postId == postId) {
                        val newHasLiked = !post.hasLiked
                        val newLikesCount = if (newHasLiked) post.likesCount + 1 else maxOf(0, post.likesCount - 1)
                        post.copy(hasLiked = newHasLiked, likesCount = newLikesCount)
                    } else {
                        post
                    }
                }
                uiState = currentState.copy(posts = updatedPosts)

                try {
                    val result = postRepository.toggleLike(postId)
                    val currentSuccessState = uiState as? ProfileUiState.Success
                    if (currentSuccessState != null) {
                        val syncedPosts = currentSuccessState.posts.map { post ->
                            if (post.postId == postId) {
                                if (post.hasLiked != result.liked) {
                                    val correctLikesCount = if (result.liked) post.likesCount + 1 else maxOf(0, post.likesCount - 1)
                                    post.copy(hasLiked = result.liked, likesCount = correctLikesCount)
                                } else {
                                    post
                                }
                            } else {
                                post
                            }
                        }
                        uiState = currentSuccessState.copy(posts = syncedPosts)
                    }
                } catch (e: Exception) {
                    val currentSuccessState = uiState as? ProfileUiState.Success
                    if (currentSuccessState != null) {
                        val revertedPosts = currentSuccessState.posts.map { post ->
                            if (post.postId == postId) {
                                val revertedHasLiked = !post.hasLiked
                                val revertedLikesCount = if (revertedHasLiked) post.likesCount + 1 else maxOf(0, post.likesCount - 1)
                                post.copy(hasLiked = revertedHasLiked, likesCount = revertedLikesCount)
                            } else {
                                post
                            }
                        }
                        uiState = currentSuccessState.copy(posts = revertedPosts)
                    }
                }
            }
        }
    }

    fun sendFriendRequest() {
        viewModelScope.launch {
            val currentState = uiState
            if (currentState is ProfileUiState.Success) {
                try {
                    val req = friendRepository.sendRequest(resolvedUserId)
                    val updatedUser = currentState.user.copy(
                        friendshipStatus = "request_sent",
                        friendRequestId = req.requestId
                    )
                    uiState = currentState.copy(user = updatedUser)
                } catch (e: Exception) {
                    // Fail silently
                }
            }
        }
    }

    fun acceptFriendRequest() {
        viewModelScope.launch {
            val currentState = uiState
            if (currentState is ProfileUiState.Success) {
                val requestId = currentState.user.friendRequestId ?: return@launch
                try {
                    friendRepository.respondToRequest(requestId, "accepted")
                    val updatedUser = currentState.user.copy(
                        friendshipStatus = "friends",
                        friendsCount = currentState.user.friendsCount + 1
                    )
                    uiState = currentState.copy(user = updatedUser)
                } catch (e: Exception) {
                    // Fail
                }
            }
        }
    }

    fun rejectFriendRequest() {
        viewModelScope.launch {
            val currentState = uiState
            if (currentState is ProfileUiState.Success) {
                val requestId = currentState.user.friendRequestId ?: return@launch
                try {
                    friendRepository.respondToRequest(requestId, "rejected")
                    val updatedUser = currentState.user.copy(
                        friendshipStatus = "none",
                        friendRequestId = null
                    )
                    uiState = currentState.copy(user = updatedUser)
                } catch (e: Exception) {
                    // Fail
                }
            }
        }
    }

    fun cancelFriendRequest() {
        viewModelScope.launch {
            val currentState = uiState
            if (currentState is ProfileUiState.Success) {
                val requestId = currentState.user.friendRequestId ?: return@launch
                try {
                    friendRepository.cancelRequest(requestId)
                    val updatedUser = currentState.user.copy(
                        friendshipStatus = "none",
                        friendRequestId = null
                    )
                    uiState = currentState.copy(user = updatedUser)
                } catch (e: Exception) {
                    // Fail
                }
            }
        }
    }

    fun removeFriend() {
        viewModelScope.launch {
            val currentState = uiState
            if (currentState is ProfileUiState.Success) {
                try {
                    friendRepository.removeFriend(resolvedUserId)
                    val updatedUser = currentState.user.copy(
                        friendshipStatus = "none",
                        friendsCount = maxOf(0, currentState.user.friendsCount - 1),
                        friendRequestId = null
                    )
                    uiState = currentState.copy(user = updatedUser)
                } catch (e: Exception) {
                    // Fail
                }
            }
        }
    }
}
