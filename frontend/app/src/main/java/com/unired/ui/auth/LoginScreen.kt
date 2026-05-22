package com.unired.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unired.R
import com.unired.ui.theme.BlueA
import com.unired.ui.theme.PrimaryButtonColor
import com.unired.ui.theme.UniRedBackground

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    loginViewModel: LoginViewModel = viewModel()
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo_unired),
            contentDescription = "Fondo del sistema",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Card(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth(0.85f),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = UniRedBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(color = Color.White)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_unired),
                    contentDescription = "logo de app",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                )

                Text(
                    text = "INICIAR SESIÓN",
                    fontSize = 25.sp
                )

                Spacer(modifier = Modifier.height(26.dp))

                loginViewModel.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                OutlinedTextField(
                    value = loginViewModel.email,
                    onValueChange = { loginViewModel.onEmailChange(it) },
                    label = { Text("Correo Electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !loginViewModel.isLoading,
                    shape = RoundedCornerShape(20.dp),
                    isError = loginViewModel.emailError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                loginViewModel.emailError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = loginViewModel.password,
                    onValueChange = { loginViewModel.onPasswordChange(it) },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !loginViewModel.isLoading,
                    shape = RoundedCornerShape(20.dp),
                    isError = loginViewModel.passwordError != null,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                loginViewModel.passwordError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 2.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "¿Eres un nuevo usuario? ")
                    TextButton(
                        onClick = onNavigateToRegister,
                        enabled = !loginViewModel.isLoading,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.defaultMinSize(minWidth = 0.dp, minHeight = 0.dp)
                    ) {
                        Text(
                            text = "Regístrate",
                            color = if (loginViewModel.isLoading) Color.Gray else BlueA,
                            style = MaterialTheme.typography.bodyMedium,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { loginViewModel.login(onLoginSuccess) },
                    enabled = !loginViewModel.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryButtonColor),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(45.dp)
                ) {
                    if (loginViewModel.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Iniciar sesión")
                    }
                }
            }
        }
    }
}