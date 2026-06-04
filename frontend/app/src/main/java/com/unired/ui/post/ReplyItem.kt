package com.unired.ui.post

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import com.unired.data.model.Reply
import com.unired.ui.components.LikeButton
import com.unired.util.DateFormatter
import com.unired.util.SessionManager

@Composable
fun ReplyItem(
    reply: Reply,
    onLikeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onProfileClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val currentUserId = SessionManager.getUserId()
    val isOwnReply = reply.userId == currentUserId

    val isNew = remember {
        val dateStr = reply.createdAt
        if (dateStr.isNullOrBlank()) false
        else {
            try {
                val date = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }.parse(dateStr.replace(".SSSZ", "Z").replace("Z", ""))
                date?.let { System.currentTimeMillis() - it.time < 5000 } ?: false
            } catch (_: Exception) {
                false
            }
        }
    }
    var isVisible by remember { mutableStateOf(!isNew) }
    LaunchedEffect(Unit) {
        if (isNew) {
            isVisible = true
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(500)) + expandVertically(animationSpec = androidx.compose.animation.core.tween(500)),
        exit = fadeOut()
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            // Reply bubble container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = reply.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.Black,
                            modifier = Modifier.clickable { onProfileClick(reply.userId) }
                        )
                        
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Opciones de respuesta",
                                    tint = Color.Gray
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                if (isOwnReply) {
                                    DropdownMenuItem(
                                        text = { Text("Eliminar") },
                                        onClick = {
                                            showMenu = false
                                            onDeleteClick()
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = reply.content,
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Footer details inside the bubble
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = DateFormatter.formatRelativeTime(reply.createdAt),
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        
                        LikeButton(
                            hasLiked = reply.hasLiked,
                            likesCount = reply.likesCount,
                            onLikeClick = onLikeClick,
                            iconSize = 14.dp,
                            fontSize = 11.sp,
                            textColor = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
