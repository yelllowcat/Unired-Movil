package com.unired.data.repository

import com.unired.data.api.ApiClient
import com.unired.data.api.PostApi
import com.unired.data.model.response.LikeResult
import com.unired.data.model.dto.Liker
import com.unired.data.model.Post
import com.unired.data.model.request.CreatePostRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class PostRepository(
    private val api: PostApi = ApiClient.retrofit.create(PostApi::class.java)
) {
    suspend fun getFeed(page: Int = 1, limit: Int = 20): List<Post> {
        return safeApiCall { api.getFeed(page, limit) }
    }

    suspend fun getPost(postId: Int): Post {
        return safeApiCall { api.getPost(postId) }
    }

    suspend fun createPost(content: String, imageFile: File? = null): Post {
        return if (imageFile != null) {
            val contentPart = content.toRequestBody("text/plain".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData(
                "image",
                imageFile.name,
                imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            )
            safeApiCall { api.createPostWithImage(contentPart, imagePart) }
        } else {
            safeApiCall { api.createPost(CreatePostRequest(content)) }
        }
    }

    suspend fun updatePost(postId: Int, content: String): Post {
        return safeApiCall { api.updatePost(postId, CreatePostRequest(content)) }
    }

    suspend fun deletePost(postId: Int) {
        safeApiCallUnit { api.deletePost(postId) }
    }

    suspend fun toggleLike(postId: Int): LikeResult {
        return safeApiCall { api.toggleLike(postId) }
    }

    suspend fun getLikers(postId: Int): List<Liker> {
        return safeApiCall { api.getLikers(postId) }
    }
}
