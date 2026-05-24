package com.unired.ui.post

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unired.data.model.Comment
import com.unired.data.model.Reply
import com.unired.util.DateFormatter
import com.unired.util.SessionManager

@Composable
fun CommentItem(
    comment: Comment,
    onLikeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onHideClick: () -> Unit,
    onReplyClick: () -> Unit,
    replies: List<Reply>,
    onReplyLikeClick: (Reply) -> Unit,
    onDeleteReplyClick: (Reply) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val currentUserId = SessionManager.getUserId()
    val isOwnComment = comment.userId == currentUserId

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Comment bubble container
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
                        text = comment.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Opciones de comentario",
                                tint = Color.Gray
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (isOwnComment) {
                                DropdownMenuItem(
                                    text = { Text("Eliminar") },
                                    onClick = {
                                        showMenu = false
                                        onDeleteClick()
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Ocultar") },
                                    onClick = {
                                        showMenu = false
                                        onHideClick()
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = comment.content,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
        
        // Footer Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = DateFormatter.formatRelativeTime(comment.createdAt),
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = "Responder",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.clickable { onReplyClick() }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onLikeClick() }
            ) {
                Icon(
                    imageVector = if (comment.hasLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Me gusta",
                    tint = if (comment.hasLiked) Color.Red else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = comment.likesCount.toString(),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
        
        if (replies.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 4.dp, end = 4.dp)
                    .height(IntrinsicSize.Min)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(Color(0xFFD3D3D3))
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    replies.forEach { reply ->
                        ReplyItem(
                            reply = reply,
                            onLikeClick = { onReplyLikeClick(reply) },
                            onDeleteClick = { onDeleteReplyClick(reply) }
                        )
                    }
                }
            }
        }
    }
}
