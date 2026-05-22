package com.unired.data.model.dto

data class Liker(
    val userId: Int,
    val fullName: String,
    val profilePicture: String?,
    val likedAt: String
)
