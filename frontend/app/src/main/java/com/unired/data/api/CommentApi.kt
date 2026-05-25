package com.unired.data.api

import com.unired.data.model.Comment
import com.unired.data.model.response.LikeResult
import com.unired.data.model.request.CreateCommentRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CommentApi {
    @GET("posts/{postId}/comments")
    suspend fun getComments(
        @Path("postId") postId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): ApiResponse<List<Comment>>

    @POST("posts/{postId}/comments")
    suspend fun createComment(
        @Path("postId") postId: Int,
        @Body request: CreateCommentRequest
    ): ApiResponse<Comment>

    @DELETE("comments/{id}")
    suspend fun deleteComment(@Path("id") commentId: Int): ApiResponse<Unit>

    @POST("comments/{id}/like")
    suspend fun toggleLike(@Path("id") commentId: Int): ApiResponse<LikeResult>

    @POST("comments/{id}/hide")
    suspend fun hideComment(@Path("id") commentId: Int): ApiResponse<Unit>
}
