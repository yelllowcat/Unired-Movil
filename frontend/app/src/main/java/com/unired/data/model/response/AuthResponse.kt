package com.unired.data.model.response

import com.unired.data.model.User

data class AuthResponse(
    val token: String,
    val user: User
)