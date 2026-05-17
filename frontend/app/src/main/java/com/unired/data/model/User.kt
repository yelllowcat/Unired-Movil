package com.unired.data.model

data class User(
    val userId: Int,
    val fullName: String,
    val biography: String?,
    val profilePicture: String,
    val email: String,
    val role: String,
    val registrationDate: String
)