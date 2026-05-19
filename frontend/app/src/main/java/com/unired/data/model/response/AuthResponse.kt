package com.unired.data.model.response

data class AuthResponse(
    val token: String,
    val user: User
)