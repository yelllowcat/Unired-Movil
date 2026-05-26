package com.unired.data.repository

import com.unired.data.api.ApiClient
import com.unired.data.api.ReplyApi
import com.unired.data.model.Reply
import com.unired.data.model.request.CreateReplyRequest
import com.unired.data.model.response.LikeResult

class ReplyRepository(private val api: ReplyApi = ApiClient.retrofit.create(ReplyApi::class.java)) {
    suspend fun getReplies(commentId: Int): List<Reply> {
        return safeApiCall { api.getReplies(commentId) }
    }

    suspend fun addReply(commentId: Int, content: String): Reply {
        return safeApiCall { api.createReply(commentId, CreateReplyRequest(content)) }
    }

    suspend fun deleteReply(replyId: Int) {
        safeApiCallUnit { api.deleteReply(replyId) }
    }

    suspend fun toggleLike(replyId: Int): LikeResult {
        return safeApiCall { api.toggleLike(replyId) }
    }
}
