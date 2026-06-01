package com.unired.ui.post

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unired.data.repository.PostRepository
import com.unired.data.repository.UserRepository
import com.unired.util.FileUtil
import com.unired.util.SessionManager
import kotlinx.coroutines.launch

class CreatePostViewModel(
    private val postId: Int? = null,
    private val postRepository: PostRepository = PostRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    var postContent by mutableStateOf("")
    var selectedImageUri by mutableStateOf<Uri?>(null)
    var existingImageUrl by mutableStateOf<String?>(null)
    var isUploading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var hadImageInitially = false

    var authorName by mutableStateOf("Cargando...")
    var authorPicture by mutableStateOf<String?>(null)

    init {
        loadUserProfile()
        loadPostToEdit()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val userId = SessionManager.getUserId()
                if (userId != -1) {
                    val user = userRepository.getProfile(userId)
                    authorName = user.fullName
                    authorPicture = user.profilePicture
                } else {
                    authorName = "Usuario UniRed"
                }
            } catch (e: Exception) {
                authorName = "Usuario UniRed"
            }
        }
    }

    fun onContentChange(content: String) {
        if (content.length <= 500) {
            postContent = content
        }
    }

    fun onImageSelected(uri: Uri?) {
        selectedImageUri = uri
        if (uri != null) {
            existingImageUrl = null
        }
    }

    fun clearError() {
        errorMessage = null
    }

    private fun loadPostToEdit() {
        if (postId != null) {
            viewModelScope.launch {
                try {
                    val post = postRepository.getPost(postId)
                    postContent = post.content
                    existingImageUrl = post.image
                    hadImageInitially = post.image != null
                } catch (e: Exception) {
                    errorMessage = "Error al cargar la publicación para editar"
                }
            }
        }
    }

    fun createPost(context: Context, onSuccess: () -> Unit) {
        if (postContent.trim().isBlank() && selectedImageUri == null) {
            errorMessage = "La publicación no puede estar vacía"
            return
        }

        viewModelScope.launch {
            isUploading = true
            errorMessage = null
            try {
                val imageFile = selectedImageUri?.let { uri ->
                    FileUtil.uriToFile(context, uri)
                }
                
                postRepository.createPost(postContent.trim(), imageFile)
                isUploading = false
                onSuccess()
            } catch (e: Exception) {
                isUploading = false
                errorMessage = e.message ?: "Ocurrió un error al crear la publicación"
            }
        }
    }

    fun updatePost(context: Context, onSuccess: () -> Unit) {
        if (postId == null) return
        if (postContent.trim().isBlank() && selectedImageUri == null && existingImageUrl == null) {
            errorMessage = "La publicación no puede estar vacía"
            return
        }

        viewModelScope.launch {
            isUploading = true
            errorMessage = null
            try {
                val removeImage = hadImageInitially && existingImageUrl == null && selectedImageUri == null
                postRepository.updatePost(postId, postContent.trim(), removeImage)
                isUploading = false
                onSuccess()
            } catch (e: Exception) {
                isUploading = false
                errorMessage = e.message ?: "Ocurrió un error al actualizar la publicación"
            }
        }
    }
}
