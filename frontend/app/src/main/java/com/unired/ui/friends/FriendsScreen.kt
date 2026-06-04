package com.unired.ui.friends

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unired.R
import com.unired.data.model.FriendRequest
import com.unired.data.model.dto.UserPreview
import com.unired.ui.components.AvatarImage
import com.unired.ui.components.LoadingIndicator
import com.unired.ui.navigation.Screen
import com.unired.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    onNavigateToProfile: (userId: Int) -> Unit,
    viewModel: FriendsViewModel = viewModel()
) {
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
                onRefresh = { viewModel.refreshFriends() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header (User Profile Name & Buscador)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = viewModel.currentUserFullName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        // Search input
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color.White, shape = RoundedCornerShape(20.dp))
                                .border(1.dp, Color.LightGray.copy(alpha = 0.5f), shape = RoundedCornerShape(20.dp))
                                .padding(start = 12.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
                                .width(180.dp)
                        ) {
                            BasicTextField(
                                value = viewModel.searchQuery,
                                onValueChange = { viewModel.onSearchQueryChange(it) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    if (viewModel.searchQuery.isEmpty()) {
                                        Text("Buscador", color = Color.Gray, fontSize = 14.sp)
                                    }
                                    innerTextField()
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color(0xFF33B5B5), shape = RoundedCornerShape(14.dp))
                                    .clickable { viewModel.performSearch() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Tabs Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FriendsTab.values().forEach { tab ->
                            val isSelected = viewModel.currentTab == tab
                            val label = when (tab) {
                                FriendsTab.SOLICITUDES -> "Solicitudes"
                                FriendsTab.ENVIAR_SOLICITUD -> "Enviar solicitud"
                                FriendsTab.PENDIENTES -> "Solicitudes pendientes"
                                FriendsTab.TODOS -> "Tus amigos"
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { viewModel.onTabChange(tab) }
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.Black else Color.Gray,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .height(2.dp)
                                        .width(if (isSelected) 60.dp else 0.dp)
                                        .background(Color(0xFF33B5B5))
                                    )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Error Message if any
                    viewModel.errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = Color.Red,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            fontSize = 14.sp
                        )
                    }

                    // Content Area
                    Box(modifier = Modifier.weight(1f)) {
                        if (viewModel.isLoading && !viewModel.isRefreshing) {
                            LoadingIndicator()
                        } else {
                            when (viewModel.currentTab) {
                                FriendsTab.SOLICITUDES -> {
                                    RequestsList(
                                        requests = viewModel.incomingRequests,
                                        isIncoming = true,
                                        onAccept = { viewModel.acceptRequest(it) },
                                        onReject = { viewModel.rejectRequest(it) },
                                        onNavigateToProfile = onNavigateToProfile
                                    )
                                }
                                FriendsTab.ENVIAR_SOLICITUD -> {
                                    SearchResultsList(
                                        users = viewModel.searchResults,
                                        onNavigateToProfile = onNavigateToProfile,
                                        onSendRequest = { viewModel.sendRequest(it) },
                                        onAcceptRequest = { viewModel.acceptRequest(it) },
                                        onRejectRequest = { viewModel.rejectRequest(it) },
                                        onCancelRequest = { viewModel.cancelRequest(it) },
                                        onRemoveFriend = { viewModel.removeFriend(it) }
                                    )
                                }
                                FriendsTab.PENDIENTES -> {
                                    RequestsList(
                                        requests = viewModel.sentRequests,
                                        isIncoming = false,
                                        onAccept = {},
                                        onReject = {},
                                        onCancel = { viewModel.cancelRequest(it) },
                                        onNavigateToProfile = onNavigateToProfile
                                    )
                                }
                                FriendsTab.TODOS -> {
                                    SearchResultsList(
                                        users = viewModel.friendsList,
                                        onNavigateToProfile = onNavigateToProfile,
                                        onSendRequest = { viewModel.sendRequest(it) },
                                        onAcceptRequest = { viewModel.acceptRequest(it) },
                                        onRejectRequest = { viewModel.rejectRequest(it) },
                                        onCancelRequest = { viewModel.cancelRequest(it) },
                                        onRemoveFriend = { viewModel.removeFriend(it) },
                                        emptyMessage = "No tienes amigos en tu lista"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestsList(
    requests: List<FriendRequest>,
    isIncoming: Boolean,
    onAccept: (requestId: Int) -> Unit,
    onReject: (requestId: Int) -> Unit,
    onCancel: (requestId: Int) -> Unit = {},
    onNavigateToProfile: (Int) -> Unit
) {
    if (requests.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isIncoming) "No tienes solicitudes de amistad" else "No tienes solicitudes pendientes",
                color = Color.Gray,
                fontSize = 15.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(requests) { request ->
                val name = request.senderName ?: "Usuario"
                val picture = request.senderPicture
                val date = DateFormatter.formatDateString(request.requestDate)
                val targetUserId = if (isIncoming) request.senderId else request.receiverId

                CardItem(
                    name = name,
                    picture = picture,
                    date = date,
                    onItemClick = { onNavigateToProfile(targetUserId) },
                    buttons = {
                        if (isIncoming) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onReject(request.requestId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .height(36.dp)
                                        .weight(1f),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Eliminar", color = Color.White, fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { onAccept(request.requestId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33B5B5)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .height(36.dp)
                                        .weight(1f),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Aceptar", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        } else {
                            Button(
                                onClick = { onCancel(request.requestId) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Cancelar solicitud", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SearchResultsList(
    users: List<UserPreview>,
    onNavigateToProfile: (userId: Int) -> Unit,
    onSendRequest: (userId: Int) -> Unit,
    onAcceptRequest: (requestId: Int) -> Unit,
    onRejectRequest: (requestId: Int) -> Unit,
    onCancelRequest: (requestId: Int) -> Unit,
    onRemoveFriend: (userId: Int) -> Unit,
    emptyMessage: String = "Busca usuarios por nombre"
) {
    if (users.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emptyMessage,
                color = Color.Gray,
                fontSize = 15.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(users) { user ->
                val date = DateFormatter.formatDateString(user.registrationDate)

                CardItem(
                    name = user.fullName,
                    picture = user.profilePicture,
                    date = date,
                    onItemClick = { onNavigateToProfile(user.userId) },
                    buttons = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            when (user.friendshipStatus ?: "none") {
                                "me" -> {
                                    Button(
                                        onClick = { onNavigateToProfile(user.userId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33B5B5)),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .height(36.dp)
                                            .fillMaxWidth(),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Ver mi perfil", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                                "friends" -> {
                                    Button(
                                        onClick = { onNavigateToProfile(user.userId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33B5B5)),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .height(36.dp)
                                            .weight(1f),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Ver perfil", color = Color.White, fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = { onRemoveFriend(user.userId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .height(36.dp)
                                            .weight(1f),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Eliminar", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                                "request_sent" -> {
                                    Button(
                                        onClick = { onNavigateToProfile(user.userId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33B5B5)),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .height(36.dp)
                                            .weight(1f),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Ver perfil", color = Color.White, fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = { user.friendRequestId?.let { onCancelRequest(it) } },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .height(36.dp)
                                            .weight(1f),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Cancelar", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                                "request_received" -> {
                                    Button(
                                        onClick = { user.friendRequestId?.let { onRejectRequest(it) } },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .height(36.dp)
                                            .weight(1f),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Eliminar", color = Color.White, fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = { user.friendRequestId?.let { onAcceptRequest(it) } },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33B5B5)),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .height(36.dp)
                                            .weight(1f),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Aceptar", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                                else -> { // none
                                    Button(
                                        onClick = { onNavigateToProfile(user.userId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33B5B5)),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .height(36.dp)
                                            .weight(1f),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Ver perfil", color = Color.White, fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = { onSendRequest(user.userId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0F2F1)),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .height(36.dp)
                                            .weight(1f),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Agregar", color = Color(0xFF00796B), fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CardItem(
    name: String,
    picture: String?,
    date: String,
    onItemClick: (() -> Unit)? = null,
    buttons: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarImage(
                imageUrl = picture,
                fullName = name,
                modifier = Modifier
                    .size(width = 120.dp, height = 75.dp)
                    .then(
                        if (onItemClick != null) Modifier.clickable { onItemClick() } else Modifier
                    ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = if (onItemClick != null) Modifier.clickable { onItemClick() } else Modifier
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Se unió el: $date",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))
                buttons()
            }
        }
    }
}

