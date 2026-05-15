package com.unired.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class LoginViewModel: ViewModel(){

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var isPasswordVisible by mutableStateOf(false)
        private set

    fun onEmailChange(newValue: String){
        email = newValue
    }

    fun onPasswordChange(newValue: String){
        password = newValue
    }

    fun togglePasswordVisibility(){
        isPasswordVisible = !isPasswordVisible
    }

    fun isLoginEnabled(): Boolean{
        return email.isNotBlank() && password.length >=8 && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun login(onSuccess: () -> Unit){
        if (isLoginEnabled()){
            onSuccess
        }
    }
}