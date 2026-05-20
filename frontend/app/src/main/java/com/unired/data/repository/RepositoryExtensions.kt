package com.unired.data.repository

import com.google.gson.Gson
import com.unired.data.api.ApiResponse
import retrofit2.HttpException
import java.io.IOException

private data class ErrorEnvelope(val success: Boolean, val message: String?)

suspend fun <T> safeApiCall(call: suspend () -> ApiResponse<T>): T {
    return try {
        val response = call()
        if (response.success && response.data != null) {
            response.data
        } else {
            throw Exception(response.message ?: "Error desconocido")
        }
    } catch (e: HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        val message = try {
            Gson().fromJson(errorBody, ErrorEnvelope::class.java)?.message
        } catch (_: Exception) { null }
        throw Exception(message ?: "Error de red: ${e.code()}")
    } catch (e: IOException) {
        throw Exception("Sin conexión a internet")
    }
}

suspend fun safeApiCallUnit(call: suspend () -> ApiResponse<Unit>) {
    try {
        val response = call()
        if (!response.success) {
            throw Exception(response.message ?: "Error desconocido")
        }
    } catch (e: HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        val message = try {
            Gson().fromJson(errorBody, ErrorEnvelope::class.java)?.message
        } catch (_: Exception) { null }
        throw Exception(message ?: "Error de red: ${e.code()}")
    } catch (e: IOException) {
        throw Exception("Sin conexión a internet")
    }
}
