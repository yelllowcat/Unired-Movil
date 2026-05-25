package com.unired.data.model

data class Notification(
    val notificationId: Int,
    val userId: Int,
    val senderId: Int,
    val senderName: String,
    val senderPicture: String?,
    val type: String, // "like", "comment", "reply", "reply_like", "comment_like", "friend_request", "friend_accept"
    val postId: Int?,
    val commentId: Int?,
    val replyId: Int?,
    val isRead: Boolean,
    val createdAt: String
)
