package com.unired.ui.post

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unired.data.model.Comment
import com.unired.data.model.Post
import com.unired.data.model.Reply
import com.unired.data.model.User
import com.unired.data.repository.CommentRepository
import com.unired.data.repository.PostRepository
import com.unired.data.repository.ReplyRepository
import com.unired.data.repository.UserRepository
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
    private val commentRepository: CommentRepository = CommentRepository(),
    private val replyRepository: ReplyRepository = ReplyRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    var uiState by mutableStateOf<PostDetailUiState>(PostDetailUiState.Loading)
        private set

    val repliesMap = androidx.compose.runtime.mutableStateMapOf<Int, List<Reply>>()
    private var currentUser: User? = null

    private var currentCommentsPage = 1
    private val commentsPageSize = 10

    var isLastCommentsPage by mutableStateOf(false)
        private set

    var isLoadingMoreComments by mutableStateOf(false)
        private set

    init {
        loadPostDetail()
        loadCurrentUserProfile()
    }

    private fun loadCurrentUserProfile() {
        viewModelScope.launch {
            try {
                val myId = com.unired.util.SessionManager.getUserId()
                if (myId != -1) {
                    currentUser = userRepository.getProfile(myId)
                }
            } catch (e: Exception) {
                // Fail silently
            }
        }
    }

    fun loadPostDetail() {
        viewModelScope.launch {
            uiState = PostDetailUiState.Loading
            currentCommentsPage = 1
            isLastCommentsPage = false
            try {
                coroutineScope {
                    val postDeferred = async { postRepository.getPost(postId) }
                    val commentsDeferred = async { commentRepository.getComments(postId, page = 1, limit = commentsPageSize) }
                    
                    val post = postDeferred.await()
                    val comments = commentsDeferred.await()
                    
                    uiState = PostDetailUiState.Success(post, comments)
                    if (comments.size < commentsPageSize) {
                        isLastCommentsPage = true
                    }
                    
                    // Auto-load replies for comments
                    comments.forEach { comment ->
                        if (comment.repliesCount > 0) {
                            loadRepliesForComment(comment.commentId)
                        }
                    }
                }
            } catch (e: Exception) {
                uiState = PostDetailUiState.Error(e.message ?: "Error al cargar la publicación")
            }
        }
    }

    fun loadNextCommentsPage() {
        if (isLoadingMoreComments || isLastCommentsPage) return
        val currentSuccessState = uiState as? PostDetailUiState.Success ?: return

        viewModelScope.launch {
            isLoadingMoreComments = true
            try {
                val nextPage = currentCommentsPage + 1
                val newComments = commentRepository.getComments(postId, page = nextPage, limit = commentsPageSize)
                if (newComments.isNotEmpty()) {
                    uiState = currentSuccessState.copy(comments = currentSuccessState.comments + newComments)
                    currentCommentsPage = nextPage
                    
                    // Auto-load replies for new comments
                    newComments.forEach { comment ->
                        if (comment.repliesCount > 0) {
                            loadRepliesForComment(comment.commentId)
                        }
                    }
                }
                if (newComments.size < commentsPageSize) {
                    isLastCommentsPage = true
                }
            } catch (_: Exception) {
                // Fail silently when loading more comments
            } finally {
                isLoadingMoreComments = false
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
                val tempId = -1 * (System.currentTimeMillis() % 1000000).toInt()
                val tempComment = Comment(
                    commentId = tempId,
                    postId = postId,
                    userId = com.unired.util.SessionManager.getUserId(),
                    content = content,
                    createdAt = java.time.Instant.now().toString(),
                    fullName = currentUser?.fullName ?: "Tú",
                    profilePicture = currentUser?.profilePicture ?: "",
                    likesCount = 0,
                    repliesCount = 0,
                    hasLiked = false
                )
                
                // Optimistic update
                val originalComments = currentState.comments
                val updatedComments = originalComments + tempComment
                val updatedPost = currentState.post.copy(commentsCount = currentState.post.commentsCount + 1)
                uiState = currentState.copy(post = updatedPost, comments = updatedComments)
                
                // Clear the text field immediately
                onSuccess()

                try {
                    val newComment = commentRepository.addComment(postId, content)
                    // Replace temp comment with real one
                    val currentSuccessState = uiState as? PostDetailUiState.Success
                    if (currentSuccessState != null) {
                        val syncedComments = currentSuccessState.comments.map {
                            if (it.commentId == tempId) newComment else it
                        }
                        uiState = currentSuccessState.copy(comments = syncedComments)
                    }
                } catch (e: Exception) {
                    // Revert on failure
                    val currentSuccessState = uiState as? PostDetailUiState.Success
                    if (currentSuccessState != null) {
                        val revertedComments = currentSuccessState.comments.filter { it.commentId != tempId }
                        val revertedPost = currentSuccessState.post.copy(
                            commentsCount = maxOf(0, currentSuccessState.post.commentsCount - 1)
                        )
                        uiState = currentSuccessState.copy(post = revertedPost, comments = revertedComments)
                    }
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

    fun loadRepliesForComment(commentId: Int) {
        viewModelScope.launch {
            try {
                val replies = replyRepository.getReplies(commentId)
                repliesMap[commentId] = replies
            } catch (e: Exception) {
                // Fail silently
            }
        }
    }

    fun addReply(commentId: Int, content: String, onSuccess: () -> Unit = {}) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val currentState = uiState
            if (currentState is PostDetailUiState.Success) {
                val tempId = -1 * (System.currentTimeMillis() % 1000000).toInt()
                val tempReply = Reply(
                    replyId = tempId,
                    commentId = commentId,
                    userId = com.unired.util.SessionManager.getUserId(),
                    content = content,
                    createdAt = java.time.Instant.now().toString(),
                    fullName = currentUser?.fullName ?: "Tú",
                    profilePicture = currentUser?.profilePicture ?: "",
                    likesCount = 0,
                    hasLiked = false
                )

                // Optimistic update of replies list
                val originalReplies = repliesMap[commentId] ?: emptyList()
                repliesMap[commentId] = originalReplies + tempReply

                // Optimistic update of repliesCount on parent comment
                val originalComments = currentState.comments
                val updatedComments = originalComments.map { comment ->
                    if (comment.commentId == commentId) {
                        comment.copy(repliesCount = comment.repliesCount + 1)
                    } else {
                        comment
                    }
                }
                uiState = currentState.copy(comments = updatedComments)
                
                // Clear the text field immediately
                onSuccess()

                try {
                    val newReply = replyRepository.addReply(commentId, content)
                    // Replace temp reply with real one
                    val currentReplies = repliesMap[commentId] ?: emptyList()
                    repliesMap[commentId] = currentReplies.map {
                        if (it.replyId == tempId) newReply else it
                    }
                } catch (e: Exception) {
                    // Revert on failure
                    val currentReplies = repliesMap[commentId] ?: emptyList()
                    repliesMap[commentId] = currentReplies.filter { it.replyId != tempId }
                    
                    val currentSuccessState = uiState as? PostDetailUiState.Success
                    if (currentSuccessState != null) {
                        val revertedComments = currentSuccessState.comments.map { comment ->
                            if (comment.commentId == commentId) {
                                comment.copy(repliesCount = maxOf(0, comment.repliesCount - 1))
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

    fun deleteReply(commentId: Int, replyId: Int) {
        viewModelScope.launch {
            val currentState = uiState
            if (currentState is PostDetailUiState.Success) {
                val originalReplies = repliesMap[commentId] ?: emptyList()
                val updatedReplies = originalReplies.filter { it.replyId != replyId }
                repliesMap[commentId] = updatedReplies
                
                // Update repliesCount on parent comment
                val updatedComments = currentState.comments.map { comment ->
                    if (comment.commentId == commentId) {
                        comment.copy(repliesCount = maxOf(0, comment.repliesCount - 1))
                    } else {
                        comment
                    }
                }
                uiState = currentState.copy(comments = updatedComments)

                try {
                    replyRepository.deleteReply(replyId)
                } catch (e: Exception) {
                    // Revert in case of failure
                    repliesMap[commentId] = originalReplies
                    val revertedComments = currentState.comments.map { comment ->
                        if (comment.commentId == commentId) {
                            comment.copy(repliesCount = comment.repliesCount)
                        } else {
                            comment
                        }
                    }
                    uiState = currentState.copy(comments = revertedComments)
                }
            }
        }
    }

    fun toggleReplyLike(commentId: Int, replyId: Int) {
        viewModelScope.launch {
            val currentState = uiState
            if (currentState is PostDetailUiState.Success) {
                val originalReplies = repliesMap[commentId] ?: emptyList()
                var originalReply: Reply? = null
                val updatedReplies = originalReplies.map { reply ->
                    if (reply.replyId == replyId) {
                        originalReply = reply
                        val newHasLiked = !reply.hasLiked
                        val newLikesCount = if (newHasLiked) reply.likesCount + 1 else maxOf(0, reply.likesCount - 1)
                        reply.copy(hasLiked = newHasLiked, likesCount = newLikesCount)
                    } else {
                        reply
                    }
                }
                repliesMap[commentId] = updatedReplies

                try {
                    val result = replyRepository.toggleLike(replyId)
                    val currentReplies = repliesMap[commentId] ?: emptyList()
                    val syncedReplies = currentReplies.map { reply ->
                        if (reply.replyId == replyId && originalReply != null) {
                            val correctLikesCount = if (result.liked) {
                                if (!originalReply!!.hasLiked) originalReply!!.likesCount + 1 else originalReply!!.likesCount
                            } else {
                                if (originalReply!!.hasLiked) originalReply!!.likesCount - 1 else originalReply!!.likesCount
                            }
                            reply.copy(hasLiked = result.liked, likesCount = correctLikesCount)
                        } else {
                            reply
                        }
                    }
                    repliesMap[commentId] = syncedReplies
                } catch (e: Exception) {
                    // Revert in case of failure
                    if (originalReply != null) {
                        val revertedReplies = (repliesMap[commentId] ?: emptyList()).map { reply ->
                            if (reply.replyId == replyId) originalReply!! else reply
                        }
                        repliesMap[commentId] = revertedReplies
                    }
                }
            }
        }
    }

    fun deletePost(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                postRepository.deletePost(postId)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Ocurrió un error al eliminar la publicación")
            }
        }
    }
}

