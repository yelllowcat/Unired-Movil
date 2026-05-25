package com.unired.ui.notifications

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unired.R
import com.unired.data.model.Notification
import com.unired.ui.components.AvatarImage
import com.unired.ui.components.EmptyState
import com.unired.ui.components.LoadingIndicator
import com.unired.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateToPostDetail: (Int) -> Unit,
    onNavigateToProfile: (Int) -> Unit,
    viewModel: NotificationsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState = viewModel.uiState
    val isRefreshing = viewModel.isRefreshing

    LaunchedEffect(Unit) {
        viewModel.refreshNotifications()
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
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Notificaciones",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 20.sp
                        )
                    },
                    actions = {
                        if (uiState is NotificationsUiState.Success && uiState.notifications.any { !it.isRead }) {
                            IconButton(onClick = { viewModel.markAllAsRead() }) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Marcar todas como leídas",
                                    tint = Color(0xFF40B6BA)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    )
                )
            }
        ) { paddingValues ->
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refreshNotifications() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (val state = uiState) {
                    is NotificationsUiState.Loading -> {
                        LoadingIndicator(modifier = Modifier.fillMaxSize())
                    }
                    is NotificationsUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.message,
                                color = Color.White,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadNotifications() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF33B5B5)
                                )
                            ) {
                                Text("Reintentar", color = Color.White)
                            }
                        }
                    }
                    is NotificationsUiState.Success -> {
                        if (state.notifications.isEmpty()) {
                            EmptyState(
                                message = "No tienes notificaciones por el momento",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(state.notifications, key = { it.notificationId }) { notification ->
                                    NotificationItem(
                                        notification = notification,
                                        onClick = {
                                            viewModel.markAsRead(notification.notificationId)
                                            when (notification.type) {
                                                "friend_request", "friend_accept" -> {
                                                    onNavigateToProfile(notification.senderId)
                                                }
                                                else -> {
                                                    notification.postId?.let { onNavigateToPostDetail(it) }
                                                }
                                            }
                                        }
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
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (notification.isRead) Color.White else Color(0xFFF0FAFA)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Unread Dot Indicator
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF40B6BA), shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Spacer(modifier = Modifier.width(16.dp))
            }

            // Sender Avatar
            AvatarImage(
                imageUrl = notification.senderPicture,
                fullName = notification.senderName,
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Notification Message and Timestamp
            Column(
                modifier = Modifier.weight(1f)
            ) {
                val message = getNotificationMessage(notification)
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = Color.Black,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = DateFormatter.formatRelativeTime(notification.createdAt),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

private fun getNotificationMessage(notification: Notification): String {
    return when (notification.type) {
        "like" -> "${notification.senderName} le dio me gusta a tu publicación"
        "comment" -> "${notification.senderName} comentó tu publicación"
        "reply" -> "${notification.senderName} respondió a tu comentario"
        "comment_like" -> "${notification.senderName} le dio me gusta a tu comentario"
        "reply_like" -> "${notification.senderName} le dio me gusta a tu respuesta"
        "friend_request" -> "${notification.senderName} te envió una solicitud de amistad"
        "friend_accept" -> "${notification.senderName} aceptó tu solicitud de amistad"
        else -> "Nueva interacción de ${notification.senderName}"
    }
}
