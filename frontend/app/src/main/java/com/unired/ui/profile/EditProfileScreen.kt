package com.unired.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.unired.R
import com.unired.ui.components.AvatarImage
import com.unired.ui.components.LoadingIndicator
import com.unired.util.DateFormatter
import com.unired.util.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    onProfileUpdated: () -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: EditProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val user = viewModel.user
    val fullName = viewModel.fullName
    val biography = viewModel.biography
    val selectedImageUri = viewModel.selectedImageUri
    val isUpdating = viewModel.isUpdating
    val errorMessage = viewModel.errorMessage

    var showDeleteDialog by remember { mutableStateOf(false) }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            viewModel.onImageSelected(uri)
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Geometric Background
        Image(
            painter = painterResource(id = R.drawable.fondo_unired),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (user == null) {
                    LoadingIndicator()
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom Back Button (top left capsule)
                        Box(
                            modifier = Modifier
                                .size(width = 64.dp, height = 48.dp)
                                .shadow(2.dp, shape = RoundedCornerShape(24.dp))
                                .background(Color.White, shape = RoundedCornerShape(24.dp))
                                .clickable { if (!isUpdating) onNavigateBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Regresar",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Header Profile Photo (centered circular avatar)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .shadow(4.dp, shape = CircleShape)
                                    .border(4.dp, Color.White, CircleShape)
                                    .clip(CircleShape)
                                    .clickable { pickMediaLauncher.launch("image/*") }
                            ) {
                                if (selectedImageUri != null) {
                                    AsyncImage(
                                        model = selectedImageUri,
                                        contentDescription = "Nueva foto de perfil",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    AvatarImage(
                                        imageUrl = user.profilePicture,
                                        fullName = user.fullName,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                
                                // Overlay "Editar" label
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(30.dp)
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .align(Alignment.BottomCenter),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Editar",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = user.fullName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.Black
                            )

                            Text(
                                text = DateFormatter.formatDateString(user.registrationDate),
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Input Label: Nombre completo
                        Text(
                            text = "Nombre completo",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )

                        // Input Container: Nombre completo
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(1.dp, shape = RoundedCornerShape(24.dp))
                                .background(Color.White, shape = RoundedCornerShape(24.dp))
                                .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            BasicTextField(
                                value = fullName,
                                onValueChange = { viewModel.onNameChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                ),
                                maxLines = 1,
                                decorationBox = { innerTextField ->
                                    if (fullName.isEmpty()) {
                                        Text(text = "Introduce tu nombre completo", color = Color.LightGray, fontSize = 14.sp)
                                    }
                                    innerTextField()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Input Label: Biografía
                        Text(
                            text = "Biografia:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )

                        // Input Container: Biografía
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(1.dp, shape = RoundedCornerShape(16.dp))
                                .background(Color.White, shape = RoundedCornerShape(16.dp))
                                .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                BasicTextField(
                                    value = biography,
                                    onValueChange = { viewModel.onBiographyChange(it) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    textStyle = TextStyle(
                                        fontSize = 14.sp,
                                        color = Color.DarkGray,
                                        lineHeight = 20.sp
                                    ),
                                    decorationBox = { innerTextField ->
                                        if (biography.isEmpty()) {
                                            Text(
                                                text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua...",
                                                color = Color.LightGray,
                                                fontSize = 14.sp,
                                                lineHeight = 20.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "${biography.length}/200",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End
                                )
                            }
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Action Buttons Column (Responsive stacked buttons)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = {
                                    viewModel.updateProfile(context) {
                                        onProfileUpdated()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33B5B5)),
                                shape = RoundedCornerShape(24.dp),
                                enabled = !isUpdating && fullName.isNotBlank()
                            ) {
                                Text(
                                    text = "Guardar cambios",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            Button(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text(
                                    text = "Eliminar cuenta",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // Delete Account Dialog
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Eliminar cuenta") },
                        text = { Text("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción borrará permanentemente todos tus datos y es irreversible.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteDialog = false
                                    SessionManager.clearSession()
                                    onAccountDeleted()
                                }
                            ) {
                                Text("Eliminar permanentemente", color = Color.Red)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                // Loading overlay
                if (isUpdating) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LoadingIndicator(modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Guardando cambios...",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
