package com.unired.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    var emailError by mutableStateOf<String?>(null)
        private set
    var passwordError by mutableStateOf<String?>(null)
        private set

    fun onEmailChange(newValue: String) {
        email = newValue
        if (emailError != null) emailError = null
    }

    fun onPasswordChange(newValue: String) {
        password = newValue
        if (passwordError != null) passwordError = null
    }

    fun validate(): Boolean {
        var isValid = true
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
        return isValid
    }

    fun login(onSuccess: () -> Unit) {
        if (validate()) {
            onSuccess()
        }
    }
}