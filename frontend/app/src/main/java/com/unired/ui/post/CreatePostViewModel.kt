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
    private val postRepository: PostRepository = PostRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    var postContent by mutableStateOf("")
    var selectedImageUri by mutableStateOf<Uri?>(null)
    var isUploading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
        private set

    var authorName by mutableStateOf("Cargando...")
    var authorPicture by mutableStateOf<String?>(null)

    init {
        loadUserProfile()
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
    }

    fun clearError() {
        errorMessage = null
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
}
