package com.unired.data.model

data class Post(
    val id: Int,
    val userName: String,
    val userPhotoRes: Int,
    val date: String,
    val postImageRes: Int? = null,
    val description: String,
    val likesCount: Int = 0,
    val commentsCount: Int = 0
)