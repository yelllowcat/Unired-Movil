package com.unired.data.api

import com.unired.data.model.Reply
import com.unired.data.model.request.CreateReplyRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ReplyApi {
    @GET("comments/{commentId}/replies")
    suspend fun getReplies(@Path("commentId") commentId: Int): ApiResponse<List<Reply>>

    @POST("comments/{commentId}/replies")
    suspend fun createReply(
        @Path("commentId") commentId: Int,
        @Body request: CreateReplyRequest
    ): ApiResponse<Reply>

    @DELETE("replies/{id}")
    suspend fun deleteReply(@Path("id") replyId: Int): ApiResponse<Unit>
}
