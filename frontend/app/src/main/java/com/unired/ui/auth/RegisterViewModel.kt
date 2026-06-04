package com.unired.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unired.data.repository.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {
    var fullName by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set

    var fullNameError by mutableStateOf<String?>(null)
        private set
    var emailError by mutableStateOf<String?>(null)
        private set
    var passwordError by mutableStateOf<String?>(null)
        private set
    var confirmPasswordError by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onFullNameChange(newValue: String) {
        if (newValue.length <= 50) {
            fullName = newValue
            if (fullNameError != null) fullNameError = null
            if (errorMessage != null) errorMessage = null
        }
    }
    fun onEmailChange(newValue: String) {
        email = newValue
        if (emailError != null) emailError = null
        if (errorMessage != null) errorMessage = null
    }
    fun onPasswordChange(newValue: String) {
        password = newValue
        if (passwordError != null) passwordError = null
        if (errorMessage != null) errorMessage = null
    }
    fun onConfirmPasswordChange(newValue: String) {
        confirmPassword = newValue
        if (confirmPasswordError != null) confirmPasswordError = null
        if (errorMessage != null) errorMessage = null
    }

    fun validate(): Boolean {
        var isValid = true
        val nameRegex = Regex("^[a-zA-ZáéíóúüñÁÉÍÓÚÜÑ\\s]+$")
        fullNameError = when {
            fullName.isBlank() -> { isValid = false; "El nombre completo es obligatorio" }
            fullName.length > 50 -> { isValid = false; "El nombre no puede exceder los 50 caracteres" }
            !nameRegex.matches(fullName) -> { isValid = false; "El nombre solo puede contener letras y espacios" }
            else -> null
        }
        emailError = when {
            email.isBlank() -> { isValid = false; "El correo es obligatorio" }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                isValid = false; "Formato de correo no válido"
            }
            !(email.trim().endsWith("@alu.uabcs.mx", ignoreCase = true) || email.trim().endsWith("@uabcs.mx", ignoreCase = true)) -> {
                isValid = false; "Solo se permiten correos con dominio @alu.uabcs.mx y @uabcs.mx"
            }
            else -> null
        }
        passwordError = when {
            password.isBlank() -> { isValid = false; "La contraseña es obligatoria" }
            password.length < 8 -> { isValid = false; "Mínimo 8 caracteres" }
            else -> null
        }
        confirmPasswordError = when {
            confirmPassword.isBlank() -> { isValid = false; "Confirme su contraseña" }
            confirmPassword != password -> { isValid = false; "Las contraseñas no coinciden" }
            else -> null
        }
        return isValid
    }

    fun register(onSuccess: () -> Unit) {
        if (validate()) {
            viewModelScope.launch {
                isLoading = true
                errorMessage = null
                try {
                    repository.register(fullName, email, password)
                    onSuccess()
                } catch (e: Exception) {
                    errorMessage = e.message ?: "Ocurrió un error inesperado"
                } finally {
                    isLoading = false
                }
            }
        }
    }
}