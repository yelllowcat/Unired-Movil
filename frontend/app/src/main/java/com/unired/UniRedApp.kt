package com.unired

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.unired.data.model.Notification
import com.unired.data.repository.NotificationRepository
import com.unired.ui.components.AvatarImage
import com.unired.ui.components.BottomNavItem
import com.unired.ui.components.UniRedBottomBar
import com.unired.ui.navigation.NavGraph
import com.unired.ui.navigation.Screen
import com.unired.util.SessionManager
import kotlinx.coroutines.delay

@Composable
fun UniRedApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val hideBaraIn = listOf(
        Screen.Login.route,
        Screen.Register.route,
        Screen.PostDetail.route,
        Screen.EditProfile.route
    )
    val showBottomBar = currentRoute !in hideBaraIn

    val bottomItems = listOf(
        BottomNavItem("Feed", Screen.Feed.route, R.drawable.ic_feed),
        BottomNavItem("newPost", Screen.CreatePost.route, R.drawable.ic_new_post),
        BottomNavItem("Friends", Screen.Friends.route, R.drawable.ic_friends),
        BottomNavItem("Profile", "profile/me", R.drawable.ic_profile)
    )

    // Notification polling and pop-up state
    val notificationRepository = remember { NotificationRepository() }
    var activeNotification by remember { mutableStateOf<Notification?>(null) }
    val displayedNotificationIds = remember { mutableStateListOf<Int>() }
    var isFirstLoad by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            if (SessionManager.isLoggedIn()) {
                try {
                    val notifications = notificationRepository.getNotifications()
                    if (isFirstLoad) {
                        // Mark existing unread notifications as "displayed" so they don't spam on app start
                        val unreadIds = notifications.filter { !it.isRead }.map { it.notificationId }
                        displayedNotificationIds.addAll(unreadIds)
                        isFirstLoad = false
                    } else {
                        // Find new unread notifications that haven't been shown yet
                        val newUnread = notifications.firstOrNull { !it.isRead && it.notificationId !in displayedNotificationIds }
                        if (newUnread != null) {
                            displayedNotificationIds.add(newUnread.notificationId)
                            activeNotification = newUnread
                            
                            // Mark as read in the backend immediately so we don't fetch it again
                            try {
                                notificationRepository.markAsRead(newUnread.notificationId)
                            } catch (_: Exception) {}
                        }
                    }
                } catch (_: Exception) {}
            }
            delay(10_000) // Poll every 10 seconds
        }
    }

    // Auto-dismiss the active notification after 5 seconds
    LaunchedEffect(activeNotification) {
        if (activeNotification != null) {
            delay(5000)
            activeNotification = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (showBottomBar){
                    UniRedBottomBar(
                        items = bottomItems,
                        currentRoute = currentRoute,
                        onItemClick = { item ->
                            // Intenta regresar (pop) a la pestaña si ya existe en la pila de navegación.
                            // De lo contrario, realiza la navegación estándar.
                            val popped = navController.popBackStack(route = item.route, inclusive = false)
                            if (!popped) {
                                navController.navigate(item.route) {
                                    popUpTo(Screen.Feed.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }

        // Custom Pop-up Notification Banner
        AnimatedVisibility(
            visible = activeNotification != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            activeNotification?.let { notif ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (notif.postId != null) {
                                navController.navigate("post_detail/${notif.postId}")
                            } else {
                                navController.navigate("profile/${notif.senderId}")
                            }
                            activeNotification = null
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarImage(
                            imageUrl = notif.senderPicture,
                            fullName = notif.senderName,
                            modifier = Modifier.size(40.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = notif.senderName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E1E1E)
                            )
                            Text(
                                text = when (notif.type) {
                                    "like" -> "le dio me gusta a tu publicación."
                                    "comment" -> "comentó tu publicación."
                                    "reply" -> "respondió a tu comentario."
                                    "reply_like" -> "le dio me gusta a tu respuesta."
                                    "comment_like" -> "le dio me gusta a tu comentario."
                                    "friend_request" -> "te envió una solicitud de amistad."
                                    "friend_accept" -> "aceptó tu solicitud de amistad."
                                    else -> "realizó una acción."
                                },
                                fontSize = 13.sp,
                                color = Color(0xFF555555)
                            )
                        }

                        IconButton(
                            onClick = { activeNotification = null }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}