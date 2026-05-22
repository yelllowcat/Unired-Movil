package com.unired.data.api

import com.unired.data.model.dto.FriendRequestDto
import com.unired.data.model.dto.UserPreview
import com.unired.data.model.request.RespondFriendRequest
import com.unired.data.model.request.SendFriendRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface FriendApi {
    @POST("friends/request")
    suspend fun sendRequest(@Body request: SendFriendRequest): ApiResponse<FriendRequestDto>

    @PUT("friends/request/{id}")
    suspend fun respondToRequest(
        @Path("id") requestId: Int,
        @Body request: RespondFriendRequest
    ): ApiResponse<Map<String, Any>>

    @GET("friends/")
    suspend fun getFriends(): ApiResponse<List<UserPreview>>

    @GET("friends/requests/pending")
    suspend fun getPendingRequests(): ApiResponse<List<FriendRequestDto>>

    @DELETE("friends/{id}")
    suspend fun removeFriend(@Path("id") friendId: Int): ApiResponse<Unit>
}
