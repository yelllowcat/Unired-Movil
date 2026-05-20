package com.unired.data.repository

import com.unired.data.api.ApiClient
import com.unired.data.api.UserApi
import com.unired.data.model.Post
import com.unired.data.model.User
import com.unired.data.model.dto.UserPreview
import com.unired.data.model.request.UpdateProfileRequest
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class UserRepository(private val api: UserApi = ApiClient.retrofit.create(UserApi::class.java)) {
    suspend fun getProfile(userId: Int): User {
        return safeApiCall { api.getProfile(userId) }
    }

    suspend fun updateProfile(
            userId: Int,
            fullName: String? = null,
            biography: String? = null,
            imageFile: File? = null
    ): User {
        return if (imageFile != null) {
            val namePart = fullName?.toRequestBody("text/plain".toMediaTypeOrNull())
            val bioPart = biography?.toRequestBody("text/plain".toMediaTypeOrNull())
            val imagePart =
                    MultipartBody.Part.createFormData(
                            "profilePicture",
                            imageFile.name,
                            imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                    )
            safeApiCall { api.updateProfileWithImage(userId, namePart, bioPart, imagePart) }
        } else {
            safeApiCall { api.updateProfile(userId, UpdateProfileRequest(fullName, biography)) }
        }
    }

    suspend fun getUserPosts(userId: Int, page: Int = 1, limit: Int = 20): List<Post> {
        return safeApiCall { api.getUserPosts(userId, page, limit) }
    }

    suspend fun searchUsers(query: String): List<UserPreview> {
        return safeApiCall { api.searchUsers(query) }
    }
}
