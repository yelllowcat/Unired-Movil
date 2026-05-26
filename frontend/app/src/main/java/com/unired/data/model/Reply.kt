package com.unired.data.model

data class Reply(
    val replyId: Int,
    val commentId: Int,
    val userId: Int,
    val content: String,
    val createdAt: String,
    val fullName: String,
    val profilePicture: String?,
    val likesCount: Int = 0,
    val hasLiked: Boolean = false
)
