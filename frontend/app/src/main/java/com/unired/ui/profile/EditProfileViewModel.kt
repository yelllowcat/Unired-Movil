package com.unired.ui.profile

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unired.data.model.User
import com.unired.data.repository.UserRepository
import com.unired.util.FileUtil
import com.unired.util.SessionManager
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    var fullName by mutableStateOf("")
    var biography by mutableStateOf("")
    var selectedImageUri by mutableStateOf<Uri?>(null)
    
    var isUpdating by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
        private set

    var user by mutableStateOf<User?>(null)
        private set

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            try {
                val userId = SessionManager.getUserId()
                if (userId != -1) {
                    val profile = userRepository.getProfile(userId)
                    user = profile
                    fullName = profile.fullName
                    biography = profile.biography ?: ""
                }
            } catch (e: Exception) {
                errorMessage = "Error al cargar el perfil"
            }
        }
    }

    fun onNameChange(name: String) {
        if (name.length <= 50) {
            fullName = name
        }
    }

    fun onBiographyChange(bio: String) {
        if (bio.length <= 200) {
            biography = bio
        }
    }

    fun onImageSelected(uri: Uri?) {
        selectedImageUri = uri
    }

    fun updateProfile(context: Context, onSuccess: () -> Unit) {
        if (fullName.trim().isBlank()) {
            errorMessage = "El nombre completo no puede estar vacío"
            return
        }

        val nameRegex = Regex("^[a-zA-ZáéíóúüñÁÉÍÓÚÜÑ\\s]+$")
        if (!nameRegex.matches(fullName)) {
            errorMessage = "El nombre solo puede contener letras y espacios"
            return
        }

        if (fullName.length > 50) {
            errorMessage = "El nombre no puede exceder los 50 caracteres"
            return
        }

        if (biography.length > 200) {
            errorMessage = "La descripción no puede exceder los 200 caracteres"
            return
        }

        viewModelScope.launch {
            isUpdating = true
            errorMessage = null
            try {
                val userId = SessionManager.getUserId()
                val imageFile = selectedImageUri?.let { uri ->
                    FileUtil.uriToFile(context, uri)
                }

                userRepository.updateProfile(
                    userId = userId,
                    fullName = fullName.trim(),
                    biography = biography.trim(),
                    imageFile = imageFile
                )
                
                isUpdating = false
                onSuccess()
            } catch (e: Exception) {
                isUpdating = false
                errorMessage = e.message ?: "Error al guardar los cambios"
            }
        }
    }
}
