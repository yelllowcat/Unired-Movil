package com.unired.data.model.dto

data class FriendRequestDto(
    val requestId: Int,
    val senderId: Int,
    val receiverId: Int,
    val status: String,
    val requestDate: String,
    val responseDate: String?,
    val sender: SenderDto?
)

data class SenderDto(
    val userId: Int,
    val fullName: String,
    val profilePicture: String?
)
