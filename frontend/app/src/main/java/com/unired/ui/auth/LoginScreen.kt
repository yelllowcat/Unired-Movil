package com.unired.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import com.unired.R
import com.unired.ui.theme.UniRedBackground
import com.unired.ui.theme.UniRedPrimary

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = viewModel(),
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Image(
            painter = painterResource(id = R.drawable.fondo_unired),
            contentDescription = "Fondo del sistema",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()

        )

        Card(
            modifier = Modifier
                //.padding(32.dp)
                .wrapContentHeight()
                .fillMaxWidth(0.75f),
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
                    modifier = Modifier.fillMaxWidth().height(180.dp)

                )

                Text(
                    text = "INICIAR SESIÓN" ,
                    fontSize = 25.sp
                )

                Spacer(modifier = Modifier.height(26.dp))

                OutlinedTextField(
                    value = loginViewModel.email,
                    onValueChange = {loginViewModel.onEmailChange(it)},
                    label = {Text("Correo Electronico")},
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    keyboardOptions = KeyboardOptions( keyboardType = KeyboardType.Email )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value =  loginViewModel.password,
                    onValueChange = { loginViewModel.onPasswordChange(it)},
                    label = {Text("Contrasenia")},
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    visualTransformation = if (loginViewModel.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password

                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "¿Eres un nuevo usuario? ",
                    )
                    TextButton(
                        onClick = onNavigateToRegister,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.defaultMinSize(minWidth = 0.dp, minHeight = 0.dp)
                    ) {
                        Text(
                            text = "Registrate",
                            color = Color.Blue,
                            style = MaterialTheme.typography.bodyMedium,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))


                Button(
                    onClick = onLoginSuccess
                ) {
                    Text("Ir al Feed")
                }

        }

    }


    }
}