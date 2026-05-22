package com.unired.data.api

import com.unired.data.model.response.LikeResult
import com.unired.data.model.dto.Liker
import com.unired.data.model.Post
import com.unired.data.model.request.CreatePostRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface PostApi {
    @GET("posts")
    suspend fun getFeed(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<List<Post>>

    @POST("posts")
    suspend fun createPost(@Body request: CreatePostRequest): ApiResponse<Post>

    @Multipart
    @POST("posts")
    suspend fun createPostWithImage(
        @Part("content") content: RequestBody,
        @Part image: MultipartBody.Part
    ): ApiResponse<Post>

    @GET("posts/{id}")
    suspend fun getPost(@Path("id") postId: Int): ApiResponse<Post>

    @PUT("posts/{id}")
    suspend fun updatePost(
        @Path("id") postId: Int,
        @Body request: CreatePostRequest
    ): ApiResponse<Post>

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") postId: Int): ApiResponse<Unit>

    @POST("posts/{id}/like")
    suspend fun toggleLike(@Path("id") postId: Int): ApiResponse<LikeResult>

    @GET("posts/{id}/likers")
    suspend fun getLikers(@Path("id") postId: Int): ApiResponse<List<Liker>>
}
