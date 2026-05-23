package com.unired.ui.post

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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    onPostCreated: () -> Unit,
    viewModel: CreatePostViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val postContent = viewModel.postContent
    val selectedImageUri = viewModel.selectedImageUri
    val isUploading = viewModel.isUploading
    val errorMessage = viewModel.errorMessage
    val authorName = viewModel.authorName
    val authorPicture = viewModel.authorPicture

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            viewModel.onImageSelected(uri)
        }
    )

    // Current date formatted like: 18/03/2025
    val currentDateStr = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Back Button (top left)
                    Box(
                        modifier = Modifier
                            .size(width = 64.dp, height = 48.dp)
                            .shadow(2.dp, shape = RoundedCornerShape(24.dp))
                            .background(Color.White, shape = RoundedCornerShape(24.dp))
                            .clickable { if (!isUploading) onNavigateBack() },
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

                    // Profile Card Container
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarImage(
                                imageUrl = authorPicture,
                                fullName = authorName,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = authorName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = "Publicado el: $currentDateStr",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Main Post Creator Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Card Header
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "¿Que estas pensando?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Black,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Comparte con nosotros",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            HorizontalDivider(color = Color.LightGray, thickness = 1.dp)

                            // Image Placeholder/Preview Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedImageUri == null) {
                                    IconButton(
                                        onClick = { pickMediaLauncher.launch("image/*") },
                                        modifier = Modifier.size(100.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = "Agregar imagen",
                                            tint = Color.DarkGray,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                } else {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        AsyncImage(
                                            model = selectedImageUri,
                                            contentDescription = "Imagen seleccionada",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        
                                        // Remove Image Button
                                        IconButton(
                                            onClick = { viewModel.onImageSelected(null) },
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .size(32.dp)
                                                .background(Color.Black.copy(alpha = 0.6f), shape = CircleShape)
                                                .align(Alignment.TopEnd)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remover imagen",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Text Editor container (gray bubble matching Figma)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    BasicTextField(
                                        value = postContent,
                                        onValueChange = { viewModel.onContentChange(it) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 80.dp, max = 150.dp),
                                        textStyle = TextStyle(
                                            fontSize = 14.sp,
                                            color = Color.DarkGray,
                                            lineHeight = 20.sp
                                        ),
                                        decorationBox = { innerTextField ->
                                            if (postContent.isEmpty()) {
                                                Text(
                                                    text = "Blandit habitasse eleifend himenaeos maecenas risus dui congue torquent, felis curae eros cubilia justo iaculis ornare...",
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
                                        text = "${postContent.length}/500",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons Row (Figma: Eliminar publicación & Guardar cambios)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.onContentChange("")
                                viewModel.onImageSelected(null)
                                onNavigateBack()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)), // Red
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = "Eliminar publicación",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.createPost(context) {
                                    onPostCreated()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33B5B5)), // Teal
                            shape = RoundedCornerShape(24.dp),
                            enabled = !isUploading && (postContent.isNotBlank() || selectedImageUri != null)
                        ) {
                            Text(
                                text = "Guardar cambios",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Loading overlay
                if (isUploading) {
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
                                text = "Publicando...",
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