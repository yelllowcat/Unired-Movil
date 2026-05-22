package com.unired.data.repository

import com.unired.data.api.ApiClient
import com.unired.data.api.FriendApi
import com.unired.data.model.FriendRequest
import com.unired.data.model.dto.FriendRequestDto
import com.unired.data.model.dto.UserPreview
import com.unired.data.model.request.RespondFriendRequest
import com.unired.data.model.request.SendFriendRequest

class FriendRepository(
        private val api: FriendApi = ApiClient.retrofit.create(FriendApi::class.java)
) {
    suspend fun getFriends(): List<UserPreview> {
        return safeApiCall { api.getFriends() }
    }

    suspend fun getPendingRequests(): List<FriendRequest> {
        val dtoList = safeApiCall { api.getPendingRequests() }
        return dtoList.map { it.toModel() }
    }

    suspend fun sendRequest(receiverId: Int): FriendRequest {
        val dto = safeApiCall { api.sendRequest(SendFriendRequest(receiverId)) }
        return dto.toModel()
    }

    suspend fun respondToRequest(requestId: Int, status: String) {
        safeApiCall { api.respondToRequest(requestId, RespondFriendRequest(status)) }
    }

    suspend fun removeFriend(friendId: Int) {
        safeApiCallUnit { api.removeFriend(friendId) }
    }

    suspend fun getSentRequests(): List<FriendRequest> {
        val dtoList = safeApiCall { api.getSentRequests() }
        return dtoList.map { it.toModel() }
    }

    suspend fun cancelRequest(requestId: Int) {
        safeApiCallUnit { api.cancelRequest(requestId) }
    }

    private fun FriendRequestDto.toModel(): FriendRequest {
        return FriendRequest(
                requestId = requestId,
                senderId = senderId,
                receiverId = receiverId,
                status = status,
                requestDate = requestDate,
                responseDate = responseDate,
                senderName = sender?.fullName,
                senderPicture = sender?.profilePicture
        )
    }
}
