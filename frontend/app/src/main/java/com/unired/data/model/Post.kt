package com.unired.data.model

data class Post(
    val postId: Int,
    val userId: Int,
    val content: String,
    val image: String?,           // nullable, maps to posts.image
    val createdAt: String,
    val authorName: String,       // from JOIN
    val authorPicture: String,    // from JOIN
    val likesCount: Int,
    val commentsCount: Int,
    val hasLiked: Boolean         // from sp_has_liked on client side
)