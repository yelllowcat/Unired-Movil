package com.unired.data.model

data class Reply(
    val replyId: Int,
    val commentId: Int,
    val userId: Int,
    val content: String,
    val createdAt: String,
    val fullName: String,
    val profilePicture: String?
)
