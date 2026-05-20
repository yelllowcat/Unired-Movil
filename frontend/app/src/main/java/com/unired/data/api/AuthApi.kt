package com.unired.data.api

import com.unired.data.model.response.AuthResponse
import com.unired.data.model.response.RegisterResponse
import com.unired.data.model.request.LoginRequest
import com.unired.data.model.request.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<RegisterResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<AuthResponse>
}
