package com.unired.ui.feed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unired.data.model.Post
import com.unired.data.repository.PostRepository
import kotlinx.coroutines.launch

sealed interface FeedUiState {
    object Loading : FeedUiState
    data class Success(val posts: List<Post>) : FeedUiState
    data class Error(val message: String) : FeedUiState
}

class FeedViewModel(
    private val postRepository: PostRepository = PostRepository()
) : ViewModel() {

    var uiState by mutableStateOf<FeedUiState>(FeedUiState.Loading)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    private var currentPage = 1
    private val pageSize = 15

    var isLastPage by mutableStateOf(false)
        private set

    var isLoadingMore by mutableStateOf(false)
        private set

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            uiState = FeedUiState.Loading
            currentPage = 1
            isLastPage = false
            try {
                val posts = postRepository.getFeed(page = 1, limit = pageSize)
                uiState = FeedUiState.Success(posts)
                if (posts.size < pageSize) {
                    isLastPage = true
                }
            } catch (e: Exception) {
                uiState = FeedUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun refreshFeed() {
        viewModelScope.launch {
            isRefreshing = true
            currentPage = 1
            isLastPage = false
            try {
                val posts = postRepository.getFeed(page = 1, limit = pageSize)
                uiState = FeedUiState.Success(posts)
                if (posts.size < pageSize) {
                    isLastPage = true
                }
            } catch (e: Exception) {
                val currentState = uiState
                if (currentState !is FeedUiState.Success) {
                    uiState = FeedUiState.Error(e.message ?: "Error al actualizar feed")
                }
            } finally {
                isRefreshing = false
            }
        }
    }

    fun loadNextPage() {
        if (isLoadingMore || isLastPage) return
        val currentSuccessState = uiState as? FeedUiState.Success ?: return

        viewModelScope.launch {
            isLoadingMore = true
            try {
                val nextPage = currentPage + 1
                val newPosts = postRepository.getFeed(page = nextPage, limit = pageSize)
                if (newPosts.isNotEmpty()) {
                    uiState = FeedUiState.Success(currentSuccessState.posts + newPosts)
                    currentPage = nextPage
                }
                if (newPosts.size < pageSize) {
                    isLastPage = true
                }
            } catch (_: Exception) {
                // Fail silently when loading more to avoid disrupting UI
            } finally {
                isLoadingMore = false
            }
        }
    }

    fun toggleLike(postId: Int) {
        viewModelScope.launch {
            val currentState = uiState
            if (currentState is FeedUiState.Success) {
                val updatedPosts = currentState.posts.map { post ->
                    if (post.postId == postId) {
                        val newHasLiked = !post.hasLiked
                        val newLikesCount = if (newHasLiked) post.likesCount + 1 else maxOf(0, post.likesCount - 1)
                        post.copy(hasLiked = newHasLiked, likesCount = newLikesCount)
                    } else {
                        post
                    }
                }
                uiState = FeedUiState.Success(updatedPosts)
            }

            try {
                val result = postRepository.toggleLike(postId)
                val postState = uiState
                if (postState is FeedUiState.Success) {
                    val syncedPosts = postState.posts.map { post ->
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
                    uiState = FeedUiState.Success(syncedPosts)
                }
            } catch (e: Exception) {
                val postState = uiState
                if (postState is FeedUiState.Success) {
                    val revertedPosts = postState.posts.map { post ->
                        if (post.postId == postId) {
                            val revertedHasLiked = !post.hasLiked
                            val revertedLikesCount = if (revertedHasLiked) post.likesCount + 1 else maxOf(0, post.likesCount - 1)
                            post.copy(hasLiked = revertedHasLiked, likesCount = revertedLikesCount)
                        } else {
                            post
                        }
                    }
                    uiState = FeedUiState.Success(revertedPosts)
                }
            }
        }
    }
}
