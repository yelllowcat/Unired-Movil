package com.unired.data.repository

import com.unired.data.api.ApiClient
import com.unired.data.api.AuthApi
import com.unired.data.model.request.LoginRequest
import com.unired.data.model.request.RegisterRequest
import com.unired.data.model.response.AuthResponse
import com.unired.util.SessionManager

class AuthRepository(private val api: AuthApi = ApiClient.retrofit.create(AuthApi::class.java)) {
    suspend fun login(email: String, password: String): AuthResponse {
        val response = safeApiCall { api.login(LoginRequest(email, password)) }
        SessionManager.saveToken(response.token)
        SessionManager.saveUserId(response.user.userId)
        return response
    }

    suspend fun register(fullName: String, email: String, password: String): AuthResponse {
        val response = safeApiCall { api.register(RegisterRequest(fullName, email, password)) }
        SessionManager.saveToken(response.token)
        SessionManager.saveUserId(response.user.userId)
        return response
    }

    fun logout() {
        SessionManager.clearSession()
    }
}
