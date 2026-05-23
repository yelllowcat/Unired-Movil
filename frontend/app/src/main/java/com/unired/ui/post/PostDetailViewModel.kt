package com.unired.ui.post

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unired.data.model.Comment
import com.unired.data.model.Post
import com.unired.data.repository.CommentRepository
import com.unired.data.repository.PostRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

sealed interface PostDetailUiState {
    object Loading : PostDetailUiState
    data class Success(val post: Post, val comments: List<Comment>) : PostDetailUiState
    data class Error(val message: String) : PostDetailUiState
}

class PostDetailViewModel(
    private val postId: Int,
    private val postRepository: PostRepository = PostRepository(),
    private val commentRepository: CommentRepository = CommentRepository()
) : ViewModel() {

    var uiState by mutableStateOf<PostDetailUiState>(PostDetailUiState.Loading)
        private set

    init {
        loadPostDetail()
    }

    fun loadPostDetail() {
        viewModelScope.launch {
            uiState = PostDetailUiState.Loading
            try {
                coroutineScope {
                    val postDeferred = async { postRepository.getPost(postId) }
                    val commentsDeferred = async { commentRepository.getComments(postId) }
                    
                    val post = postDeferred.await()
                    val comments = commentsDeferred.await()
                    
                    uiState = PostDetailUiState.Success(post, comments)
                }
            } catch (e: Exception) {
                uiState = PostDetailUiState.Error(e.message ?: "Error al cargar la publicación")
            }
        }
    }

    fun togglePostLike() {
        viewModelScope.launch {
            val currentState = uiState
            if (currentState is PostDetailUiState.Success) {
                val post = currentState.post
                val newHasLiked = !post.hasLiked
                val newLikesCount = if (newHasLiked) post.likesCount + 1 else maxOf(0, post.likesCount - 1)
                val updatedPost = post.copy(hasLiked = newHasLiked, likesCount = newLikesCount)
                uiState = currentState.copy(post = updatedPost)

                try {
                    val result = postRepository.toggleLike(postId)
                    val currentSuccessState = uiState as? PostDetailUiState.Success
                    if (currentSuccessState != null) {
                        val syncedPost = currentSuccessState.post.copy(
                            hasLiked = result.liked,
                            likesCount = if (result.liked) {
                                if (!post.hasLiked) post.likesCount + 1 else post.likesCount
                            } else {
                                if (post.hasLiked) post.likesCount - 1 else post.likesCount
                            }
                        )
                        uiState = currentSuccessState.copy(post = syncedPost)
                    }
                } catch (e: Exception) {
                    val currentSuccessState = uiState as? PostDetailUiState.Success
                    if (currentSuccessState != null) {
                        val revertedPost = currentSuccessState.post.copy(
                            hasLiked = post.hasLiked,
                            likesCount = post.likesCount
                        )
                        uiState = currentSuccessState.copy(post = revertedPost)
                    }
                }
            }
        }
    }

    fun toggleCommentLike(commentId: Int) {
        viewModelScope.launch {
            val currentState = uiState
            if (currentState is PostDetailUiState.Success) {
                var originalComment: Comment? = null
                val updatedComments = currentState.comments.map { comment ->
                    if (comment.commentId == commentId) {
                        originalComment = comment
                        val newHasLiked = !comment.hasLiked
                        val newLikesCount = if (newHasLiked) comment.likesCount + 1 else maxOf(0, comment.likesCount - 1)
                        comment.copy(hasLiked = newHasLiked, likesCount = newLikesCount)
                    } else {
                        comment
                    }
                }
                uiState = currentState.copy(comments = updatedComments)

                try {
                    val result = commentRepository.toggleLike(commentId)
                    val currentSuccessState = uiState as? PostDetailUiState.Success
                    if (currentSuccessState != null) {
                        val syncedComments = currentSuccessState.comments.map { comment ->
                            if (comment.commentId == commentId) {
                                val correctLikesCount = if (result.liked) {
                                    if (!originalComment!!.hasLiked) originalComment!!.likesCount + 1 else originalComment!!.likesCount
                                } else {
                                    if (originalComment!!.hasLiked) originalComment!!.likesCount - 1 else originalComment!!.likesCount
                                }
                                comment.copy(hasLiked = result.liked, likesCount = correctLikesCount)
                            } else {
                                comment
                            }
                        }
                        uiState = currentSuccessState.copy(comments = syncedComments)
                    }
                } catch (e: Exception) {
                    val currentSuccessState = uiState as? PostDetailUiState.Success
                    if (currentSuccessState != null && originalComment != null) {
                        val revertedComments = currentSuccessState.comments.map { comment ->
                            if (comment.commentId == commentId) {
                                originalComment!!
                            } else {
                                comment
                            }
                        }
                        uiState = currentSuccessState.copy(comments = revertedComments)
                    }
                }
            }
        }
    }

    fun addComment(content: String, onSuccess: () -> Unit = {}) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val currentState = uiState
            if (currentState is PostDetailUiState.Success) {
                try {
                    val newComment = commentRepository.addComment(postId, content)
                    val updatedComments = currentState.comments + newComment
                    val updatedPost = currentState.post.copy(commentsCount = currentState.post.commentsCount + 1)
                    uiState = currentState.copy(post = updatedPost, comments = updatedComments)
                    onSuccess()
                } catch (e: Exception) {
                    // Fail silently or handle
                }
            }
        }
    }

    fun deleteComment(commentId: Int) {
        viewModelScope.launch {
            val currentState = uiState
            if (currentState is PostDetailUiState.Success) {
                val originalComments = currentState.comments
                val commentToDelete = originalComments.find { it.commentId == commentId } ?: return@launch
                
                val updatedComments = originalComments.filter { it.commentId != commentId }
                val updatedPost = currentState.post.copy(commentsCount = maxOf(0, currentState.post.commentsCount - 1))
                uiState = currentState.copy(post = updatedPost, comments = updatedComments)

                try {
                    commentRepository.deleteComment(commentId)
                } catch (e: Exception) {
                    val currentSuccessState = uiState as? PostDetailUiState.Success
                    if (currentSuccessState != null) {
                        uiState = currentSuccessState.copy(
                            post = currentSuccessState.post.copy(commentsCount = currentState.post.commentsCount),
                            comments = originalComments
                        )
                    }
                }
            }
        }
    }

    fun hideComment(commentId: Int) {
        viewModelScope.launch {
            val currentState = uiState
            if (currentState is PostDetailUiState.Success) {
                val originalComments = currentState.comments
                
                // Optimistically remove comment from view
                val updatedComments = originalComments.filter { it.commentId != commentId }
                uiState = currentState.copy(comments = updatedComments)

                try {
                    commentRepository.hideComment(commentId)
                } catch (e: Exception) {
                    val currentSuccessState = uiState as? PostDetailUiState.Success
                    if (currentSuccessState != null) {
                        uiState = currentSuccessState.copy(comments = originalComments)
                    }
                }
            }
        }
    }
}
