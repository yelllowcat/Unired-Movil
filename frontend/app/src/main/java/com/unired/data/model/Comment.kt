package com.unired.data.model

data class Comment(
    val commentId: Int,
    val postId: Int,
    val userId: Int,
    val content: String,
    val createdAt: String,
    val fullName: String,
    val profilePicture: String,
    val likesCount: Int = 0,      // from comment_likes
    val repliesCount: Int = 0,
    val hasLiked: Boolean = false
)