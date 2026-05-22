package com.unired.data.api

import com.unired.data.model.Post
import com.unired.data.model.User
import com.unired.data.model.dto.UserPreview
import com.unired.data.model.request.UpdateProfileRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {
    @GET("users/{id}")
    suspend fun getProfile(@Path("id") userId: Int): ApiResponse<User>

    @PUT("users/{id}")
    suspend fun updateProfile(
        @Path("id") userId: Int,
        @Body request: UpdateProfileRequest
    ): ApiResponse<User>

    @Multipart
    @PUT("users/{id}")
    suspend fun updateProfileWithImage(
        @Path("id") userId: Int,
        @Part("fullName") fullName: RequestBody?,
        @Part("biography") biography: RequestBody?,
        @Part profilePicture: MultipartBody.Part?
    ): ApiResponse<User>

    @GET("users/{id}/posts")
    suspend fun getUserPosts(
        @Path("id") userId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<List<Post>>

    @GET("users/search")
    suspend fun searchUsers(@Query("q") query: String): ApiResponse<List<UserPreview>>
}
