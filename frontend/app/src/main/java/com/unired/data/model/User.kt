package com.unired.data.model

data class User(
    val userId: Int,
    val fullName: String,
    val biography: String?,
    val profilePicture: String,
    val email: String,
    val role: String,
    val registrationDate: String,
    val friendsCount: Int = 0,
    val postsCount: Int = 0,
    val likesCount: Int = 0,
    val friendshipStatus: String = "none",
    val friendRequestId: Int? = null
)