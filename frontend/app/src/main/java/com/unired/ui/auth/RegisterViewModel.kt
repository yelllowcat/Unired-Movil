package com.unired.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class RegisterViewModel : ViewModel() {
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

    fun onFullNameChange(newValue: String) {
        fullName = newValue
        if (fullNameError != null) fullNameError = null
    }
    fun onEmailChange(newValue: String) {
        email = newValue
        if (emailError != null) emailError = null
    }
    fun onPasswordChange(newValue: String) {
        password = newValue
        if (passwordError != null) passwordError = null
    }
    fun onConfirmPasswordChange(newValue: String) {
        confirmPassword = newValue
        if (confirmPasswordError != null) confirmPasswordError = null
    }

    fun validate(): Boolean {
        var isValid = true
        fullNameError = if (fullName.isBlank()) {
            isValid = false; "El nombre completo es obligatorio"
        } else null
        emailError = when {
            email.isBlank() -> { isValid = false; "El correo es obligatorio" }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                isValid = false; "Formato de correo no válido"
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
            onSuccess()
        }
    }
}