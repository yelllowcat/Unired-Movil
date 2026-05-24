package com.unired.data.model.dto

data class UserPreview(
    val userId: Int,
    val fullName: String,
    val profilePicture: String?,
    val biography: String? = null,
    val registrationDate: String? = null,
    val friendshipStatus: String = "none",
    val friendRequestId: Int? = null
)
