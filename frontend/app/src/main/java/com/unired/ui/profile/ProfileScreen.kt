package com.unired.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.unired.R
import com.unired.ui.components.AvatarImage
import com.unired.ui.components.LoadingIndicator
import com.unired.ui.feed.PostCard
import com.unired.util.DateFormatter
import com.unired.util.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    onNavigateToPostDetail: (Int) -> Unit,
    onNavigateToProfile: (Int) -> Unit,
    onLogout: () -> Unit,
    onEditProfileClick: () -> Unit,
    viewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        key = userId,
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(userId) as T
            }
        }
    )
) {
    val uiState = viewModel.uiState
    var showLogoutDialog by remember { mutableStateOf(false) }

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
            PullToRefreshBox(
                isRefreshing = viewModel.isRefreshing,
                onRefresh = { viewModel.refreshProfile() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    when (uiState) {
                        is ProfileUiState.Loading -> {
                            if (!viewModel.isRefreshing) {
                                LoadingIndicator()
                            }
                        }
                    is ProfileUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = uiState.message,
                                color = Color.White,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 16.dp),
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { viewModel.loadProfile() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF33B5B5)
                                )
                            ) {
                                Text("Reintentar")
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    showLogoutDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFC62828)
                                )
                            ) {
                                Text("Cerrar Sesión")
                            }
                        }
                    }
                    is ProfileUiState.Success -> {
                        val user = uiState.user
                        val posts = uiState.posts
                        val isMe = user.friendshipStatus == "me"

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            // Top Profile card layout
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp)
                                ) {
                                    // Logout button (only if it's my own profile)
                                    if (isMe) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(top = 16.dp, end = 8.dp)
                                                .size(48.dp)
                                                .shadow(2.dp, shape = RoundedCornerShape(12.dp))
                                                .background(Color.White, shape = RoundedCornerShape(12.dp))
                                                .clickable {
                                                    showLogoutDialog = true
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                                contentDescription = "Cerrar sesión",
                                                tint = Color.Black,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }

                                    // White Card overlapping avatar
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 60.dp), // Shift card down
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Spacer(modifier = Modifier.height(50.dp)) // space for avatar bottom half

                                            Text(
                                                text = user.fullName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp,
                                                color = Color.Black
                                            )
                                            
                                            // Handle dates format like "25-octubre-2025" or similar from registrationDate
                                            val formattedDate = remember(user.registrationDate) {
                                                DateFormatter.formatDateString(user.registrationDate)
                                            }
                                            Text(
                                                text = formattedDate,
                                                fontSize = 14.sp,
                                                color = Color.Gray
                                            )

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Text(
                                                text = user.biography ?: "Sin biografía.",
                                                fontSize = 14.sp,
                                                color = Color.DarkGray,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )

                                            Spacer(modifier = Modifier.height(20.dp))

                                            // Profile Stats (Publicaciones, Amigos, Megustas)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceEvenly
                                            ) {
                                                StatColumn(value = user.postsCount.toString(), label = "Publicaciones")
                                                StatColumn(value = user.friendsCount.toString(), label = "Amigos")
                                                StatColumn(value = user.likesCount.toString(), label = "Megustas")
                                            }

                                            Spacer(modifier = Modifier.height(24.dp))

                                            // Contextual action button
                                            when (user.friendshipStatus) {
                                                "me" -> {
                                                    Button(
                                                        onClick = onEditProfileClick,
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33B5B5)),
                                                        shape = RoundedCornerShape(24.dp),
                                                        modifier = Modifier
                                                            .fillMaxWidth(0.8f)
                                                            .height(44.dp)
                                                    ) {
                                                        Text(text = "Editar Perfil", color = Color.White, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                "friends" -> {
                                                    Button(
                                                        onClick = { viewModel.removeFriend() },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                                        shape = RoundedCornerShape(24.dp),
                                                        modifier = Modifier
                                                            .fillMaxWidth(0.8f)
                                                            .height(44.dp)
                                                    ) {
                                                        Text(text = "Eliminar amigo", color = Color.White, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                "request_sent" -> {
                                                    Button(
                                                        onClick = { viewModel.cancelFriendRequest() },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                                        shape = RoundedCornerShape(24.dp),
                                                        modifier = Modifier
                                                            .fillMaxWidth(0.8f)
                                                            .height(44.dp)
                                                    ) {
                                                        Text(text = "Cancelar solicitud", color = Color.White, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                "request_received" -> {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(0.9f),
                                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                                    ) {
                                                        Button(
                                                            onClick = { viewModel.rejectFriendRequest() },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                                            shape = RoundedCornerShape(24.dp),
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .height(44.dp)
                                                        ) {
                                                            Text(text = "Rechazar amistad", color = Color.White, fontWeight = FontWeight.Bold)
                                                        }

                                                        Button(
                                                            onClick = { viewModel.acceptFriendRequest() },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33B5B5)),
                                                            shape = RoundedCornerShape(24.dp),
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .height(44.dp)
                                                        ) {
                                                            Text(text = "Aceptar amistad", color = Color.White, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                                "none" -> {
                                                    Button(
                                                        onClick = { viewModel.sendFriendRequest() },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33B5B5)),
                                                        shape = RoundedCornerShape(24.dp),
                                                        modifier = Modifier
                                                            .fillMaxWidth(0.8f)
                                                            .height(44.dp)
                                                    ) {
                                                        Text(text = "Agregar amigo", color = Color.White, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Overlapping Avatar (aligned top center)
                                    AvatarImage(
                                        imageUrl = user.profilePicture,
                                        fullName = user.fullName,
                                        modifier = Modifier
                                            .size(110.dp)
                                            .align(Alignment.TopCenter)
                                            .shadow(4.dp, shape = CircleShape)
                                            .border(4.dp, Color.White, CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            // User's Posts list
                            if (posts.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No hay publicaciones de este usuario aún.",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            } else {
                                items(
                                    items = posts,
                                    key = { it.postId }
                                ) { post ->
                                    PostCard(
                                        post = post,
                                        onLikeClick = { viewModel.togglePostLike(post.postId) },
                                        onCommentClick = { onNavigateToPostDetail(post.postId) },
                                        onProfileClick = onNavigateToProfile,
                                        onPostClick = onNavigateToPostDetail,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showLogoutDialog) {
            Dialog(onDismissRequest = { showLogoutDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F6F9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Cerrar sesión",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "¿Estás seguro de que deseas cerrar sesión?",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
                        DialogActionRow(
                            text = "Cerrar sesión",
                            textColor = Color(0xFFC62828),
                            onClick = {
                                showLogoutDialog = false
                                SessionManager.clearSession()
                                onLogout()
                            }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
                        DialogActionRow(
                            text = "Cancelar",
                            textColor = Color.Black,
                            onClick = { showLogoutDialog = false }
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
private fun StatColumn(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Black
        )
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun DialogActionRow(
    text: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )
    }
}