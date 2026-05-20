package com.unired.data.repository

import com.unired.data.api.ApiClient
import com.unired.data.api.CommentApi
import com.unired.data.model.Comment
import com.unired.data.model.response.LikeResult
import com.unired.data.model.request.CreateCommentRequest

class CommentRepository(
        private val api: CommentApi = ApiClient.retrofit.create(CommentApi::class.java)
) {
    suspend fun getComments(postId: Int): List<Comment> {
        return safeApiCall { api.getComments(postId) }
    }

    suspend fun addComment(postId: Int, content: String): Comment {
        return safeApiCall { api.createComment(postId, CreateCommentRequest(content)) }
    }

    suspend fun deleteComment(commentId: Int) {
        safeApiCallUnit { api.deleteComment(commentId) }
    }

    suspend fun toggleLike(commentId: Int): LikeResult {
        return safeApiCall { api.toggleLike(commentId) }
    }

    suspend fun hideComment(commentId: Int) {
        safeApiCallUnit { api.hideComment(commentId) }
    }
}
