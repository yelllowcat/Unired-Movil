package com.unired.ui.notifications

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unired.R
import com.unired.data.model.Notification
import com.unired.ui.components.AvatarImage
import com.unired.ui.components.EmptyState
import com.unired.ui.components.LoadingIndicator
import com.unired.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateToPostDetail: (postId: Int) -> Unit,
    onNavigateToProfile: (userId: Int) -> Unit,
    viewModel: NotificationsViewModel = viewModel()
) {
    val unreadNotifications = viewModel.notifications.filter { !it.isRead }
    val readNotifications = viewModel.notifications.filter { it.isRead }
    val hasUnread = unreadNotifications.isNotEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        // Geometric background — matches FriendsScreen / ProfileScreen pattern
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
                onRefresh = { viewModel.refreshNotifications() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // ── Header ──────────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notificaciones",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        if (hasUnread) {
                            TextButton(
                                onClick = { viewModel.markAllAsRead() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Marcar todo",
                                    color = Color(0xFF33B5B5),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // ── Content ─────────────────────────────────────────────
                    Box(modifier = Modifier.weight(1f)) {
                        if (viewModel.isLoading && !viewModel.isRefreshing) {
                            LoadingIndicator()
                        } else if (viewModel.notifications.isEmpty()) {
                            EmptyState(
                                message = "No tienes notificaciones",
                                icon = Icons.Outlined.Notifications
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // ── Unread section ────────────────────────
                                if (unreadNotifications.isNotEmpty()) {
                                    itemsIndexed(
                                        items = unreadNotifications,
                                        key = { _, it -> it.notificationId }
                                    ) { index, notification ->
                                        NotificationRow(
                                            notification = notification,
                                            onClick = {
                                                viewModel.markAsRead(notification.notificationId)
                                                if (notification.postId != null) {
                                                    onNavigateToPostDetail(notification.postId)
                                                } else {
                                                    onNavigateToProfile(notification.senderId)
                                                }
                                            }
                                        )
                                        // Trigger loading next page when close to the bottom if there are no read notifications
                                        if (readNotifications.isEmpty() && index >= unreadNotifications.lastIndex - 2) {
                                            LaunchedEffect(index) {
                                                viewModel.loadNextPage()
                                            }
                                        }
                                    }
                                }

                                // ── Read section ─────────────────────────
                                if (readNotifications.isNotEmpty()) {
                                    if (unreadNotifications.isNotEmpty()) {
                                        item {
                                            SectionDivider(label = "Anteriores")
                                        }
                                    }
                                    itemsIndexed(
                                        items = readNotifications,
                                        key = { _, it -> it.notificationId }
                                    ) { index, notification ->
                                        NotificationRow(
                                            notification = notification,
                                            onClick = {
                                                if (notification.postId != null) {
                                                    onNavigateToPostDetail(notification.postId)
                                                } else {
                                                    onNavigateToProfile(notification.senderId)
                                                }
                                            }
                                        )
                                        // Trigger loading next page when close to the bottom of read notifications
                                        if (index >= readNotifications.lastIndex - 2) {
                                            LaunchedEffect(index) {
                                                viewModel.loadNextPage()
                                            }
                                        }
                                    }
                                }

                                // ── Paging loader ─────────────────────────
                                if (viewModel.isPageLoading) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = Color(0xFF33B5B5)
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
    }
}

// ── Notification row card ──────────────────────────────────────────────────────

@Composable
private fun NotificationRow(
    notification: Notification,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color.White else Color(0xFFE8F4FD)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Unread indicator dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (!notification.isRead) Color(0xFF2196F3) else Color.Transparent,
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Sender avatar
            AvatarImage(
                imageUrl = notification.senderPicture,
                fullName = notification.senderName,
                modifier = Modifier.size(44.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.senderName,
                    fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E1E1E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notificationText(notification.type),
                    fontSize = 13.sp,
                    color = if (!notification.isRead) Color(0xFF333333) else Color(0xFF777777),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Relative time
            Text(
                text = DateFormatter.formatRelativeTime(notification.createdAt),
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

// ── Section divider ────────────────────────────────────────────────────────────

@Composable
private fun SectionDivider(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color.LightGray.copy(alpha = 0.6f)
        )
        Text(
            text = "  $label  ",
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color.LightGray.copy(alpha = 0.6f)
        )
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────────

private fun notificationText(type: String): String = when (type) {
    "like"           -> "le dio me gusta a tu publicación."
    "comment"        -> "comentó tu publicación."
    "reply"          -> "respondió a tu comentario."
    "reply_like"     -> "le dio me gusta a tu respuesta."
    "comment_like"   -> "le dio me gusta a tu comentario."
    "friend_request" -> "te envió una solicitud de amistad."
    "friend_accept"  -> "aceptó tu solicitud de amistad."
    else             -> "realizó una acción."
}
