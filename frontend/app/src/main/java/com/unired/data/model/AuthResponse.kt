package com.unired.data.model

data class AuthResponse(
    val token: String,
    val user: User
)