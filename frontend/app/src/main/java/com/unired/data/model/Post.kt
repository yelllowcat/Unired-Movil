package com.unired.data.model

data class Post(
    val postId: Int,
    val userId: Int,
    val content: String,
    val image: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val authorName: String,
    val authorPicture: String? = null,
    val authorEmail: String,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val hasLiked: Boolean = false
)