package com.unired.data.model

data class FriendRequest(
    val requestId: Int,
    val senderId: Int,
    val receiverId: Int,
    val status: String,           // "pending" | "accepted" | "rejected"
    val requestDate: String,
    val responseDate: String?,
    val senderName: String?,      // populated by backend JOIN
    val senderPicture: String?
)