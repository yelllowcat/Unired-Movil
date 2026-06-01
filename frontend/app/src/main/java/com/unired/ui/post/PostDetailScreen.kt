package com.unired.ui.post

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.unired.R
import com.unired.data.api.ApiClient
import com.unired.data.model.Comment
import com.unired.data.model.Reply
import com.unired.ui.components.AvatarImage
import com.unired.ui.components.FullScreenImageViewer
import com.unired.ui.components.LikeButton
import com.unired.ui.components.LoadingIndicator
import com.unired.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: Int,
    onNavigateToProfile: (Int) -> Unit,
    onBack: () -> Unit,
    onNavigateToEditPost: (Int) -> Unit,
    viewModel: PostDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return PostDetailViewModel(postId) as T
            }
        }
    )
) {
    val uiState = viewModel.uiState
    val context = androidx.compose.ui.platform.LocalContext.current
    var optionsState by remember { mutableStateOf(PostOptionsState.CLOSED) }
    var commentText by remember { mutableStateOf(CommentDraftManager.getDraft(postId)) }
    var activeReplyingComment by remember { mutableStateOf<Comment?>(null) }
    var isImageViewerOpen by remember { mutableStateOf(false) }
    val serverUrl = ApiClient.BASE_URL.substringBefore("/api/")

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
                // Completely transparent topBar containing only a circular back arrow
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .padding(start = 8.dp, top = 8.dp)
                                .size(40.dp)
                                .shadow(2.dp, shape = CircleShape)
                                .background(Color.White, shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Regresar",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            bottomBar = {
                if (uiState is PostDetailUiState.Success) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding(),
                        color = Color.White,
                        tonalElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (activeReplyingComment != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF5F5F5))
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Respondiendo a ${activeReplyingComment!!.fullName}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    IconButton(
                                        onClick = { activeReplyingComment = null },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Cancelar respuesta",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextField(
                                    value = commentText,
                                    onValueChange = { 
                                        commentText = it
                                        CommentDraftManager.saveDraft(postId, it)
                                    },
                                    placeholder = { Text(if (activeReplyingComment != null) "Responder" else "Comentar", color = Color.Gray) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFF5F5F5),
                                        unfocusedContainerColor = Color(0xFFF5F5F5),
                                        disabledContainerColor = Color(0xFFF5F5F5),
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    maxLines = 4,
                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Send
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onSend = {
                                            if (commentText.isNotBlank()) {
                                                val text = commentText
                                                val replyTo = activeReplyingComment
                                                if (replyTo != null) {
                                                    viewModel.addReply(replyTo.commentId, text) {
                                                        commentText = ""
                                                        activeReplyingComment = null
                                                    }
                                                } else {
                                                    viewModel.addComment(text) {
                                                        commentText = ""
                                                        CommentDraftManager.clearDraft(postId)
                                                    }
                                                }
                                            }
                                        }
                                    )
                                )

                                IconButton(
                                    onClick = {
                                        if (commentText.isNotBlank()) {
                                            val text = commentText
                                            val replyTo = activeReplyingComment
                                            if (replyTo != null) {
                                                viewModel.addReply(replyTo.commentId, text) {
                                                    commentText = ""
                                                    activeReplyingComment = null
                                                }
                                            } else {
                                                viewModel.addComment(text) {
                                                    commentText = ""
                                                    CommentDraftManager.clearDraft(postId)
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color(0xFFE0E0E0), shape = CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Enviar comentario",
                                        tint = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (uiState) {
                    is PostDetailUiState.Loading -> {
                        LoadingIndicator()
                    }
                    is PostDetailUiState.Error -> {
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
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Button(
                                onClick = { viewModel.loadPostDetail() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF33B5B5)
                                )
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                    is PostDetailUiState.Success -> {
                        val post = uiState.post
                        val comments = uiState.comments

                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Flat, edge-to-edge Post Details section matching Figma
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White)
                                ) {
                                    // Post Header Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AvatarImage(
                                            imageUrl = post.authorPicture,
                                            fullName = post.authorName,
                                            modifier = Modifier
                                                .size(45.dp)
                                                .clip(CircleShape)
                                                .clickable { onNavigateToProfile(post.userId) }
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = post.authorName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color.Black
                                            )
                                            Text(
                                                text = "Publicado el: " + DateFormatter.formatDateString(post.createdAt),
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }

                                        val currentUserId = remember { com.unired.util.SessionManager.getUserId() }
                                        if (post.userId == currentUserId) {
                                            IconButton(onClick = { optionsState = PostOptionsState.OPTIONS }) {
                                                Icon(
                                                    imageVector = Icons.Default.Menu,
                                                    contentDescription = "Opciones",
                                                    tint = Color.Black
                                                )
                                            }
                                        }
                                    }

                                    // Flat full-width post image
                                    post.image?.let { imgUrl ->
                                        val fullUrl = if (imgUrl.startsWith("http")) imgUrl else "$serverUrl$imgUrl"
                                        AsyncImage(
                                            model = fullUrl,
                                            contentDescription = "Imagen de publicación",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 300.dp)
                                                .clickable { isImageViewerOpen = true },
                                            contentScale = ContentScale.FillWidth
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        if (isImageViewerOpen) {
                                            FullScreenImageViewer(
                                                imageUrl = fullUrl,
                                                onDismiss = { isImageViewerOpen = false }
                                            )
                                        }
                                    }

                                    // Content text below image
                                    Text(
                                        text = post.content,
                                        fontSize = 14.sp,
                                        color = Color.DarkGray,
                                        lineHeight = 20.sp,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Action Bar separated by thin horizontal borders
                                    HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        LikeButton(
                                            hasLiked = post.hasLiked,
                                            likesCount = post.likesCount,
                                            onLikeClick = { viewModel.togglePostLike() },
                                            useCustomPostIcons = true
                                        )

                                        Spacer(modifier = Modifier.width(32.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_coment),
                                                contentDescription = "Comentarios",
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = post.commentsCount.toString(),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.DarkGray
                                            )
                                        }
                                    }
                                    
                                    HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Comments lazy list
                            if (comments.isEmpty()) {
                                item {
                                    // Animated bouncing offset for a subtle micro-animation on the downward arrow
                                    val infiniteTransition = rememberInfiniteTransition(label = "arrow_bounce")
                                    val dy by infiniteTransition.animateFloat(
                                        initialValue = 0f,
                                        targetValue = 6f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "dy"
                                    )

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 24.dp, vertical = 24.dp)
                                            .shadow(8.dp, shape = RoundedCornerShape(24.dp)),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White
                                        ),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = Color.LightGray.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 24.dp, vertical = 32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            // Glowing Icon Container
                                            Box(
                                                modifier = Modifier
                                                    .size(72.dp)
                                                    .background(
                                                        color = Color(0xFF40B6BA).copy(alpha = 0.1f),
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_coment),
                                                    contentDescription = null,
                                                    tint = Color(0xFF40B6BA),
                                                    modifier = Modifier.size(36.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(20.dp))

                                            // Main Text
                                            Text(
                                                text = "Sin comentarios aún",
                                                color = Color.Black,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Supporting Text
                                            Text(
                                                text = "¡Sé el primero en compartir tu opinión! Escribe tu comentario en la parte inferior.",
                                                color = Color.DarkGray,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Normal,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 20.sp
                                            )

                                            Spacer(modifier = Modifier.height(24.dp))

                                            // Bouncing arrow pointer helper pointing down
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = Color(0xFF40B6BA),
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .offset(y = dy.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                itemsIndexed(
                                    items = comments,
                                    key = { _, comment -> comment.commentId }
                                ) { index, comment ->
                                    CommentItem(
                                        comment = comment,
                                        onLikeClick = { viewModel.toggleCommentLike(comment.commentId) },
                                        onDeleteClick = { viewModel.deleteComment(comment.commentId) },
                                        onHideClick = { viewModel.hideComment(comment.commentId) },
                                        onReplyClick = {
                                            activeReplyingComment = comment
                                        },
                                        replies = viewModel.repliesMap[comment.commentId] ?: emptyList(),
                                        onReplyLikeClick = { reply ->
                                            viewModel.toggleReplyLike(comment.commentId, reply.replyId)
                                        },
                                        onDeleteReplyClick = { reply ->
                                            viewModel.deleteReply(comment.commentId, reply.replyId)
                                        },
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                    )

                                    // Trigger loading next comments page when near the end of comments
                                    if (index >= comments.lastIndex - 2) {
                                        LaunchedEffect(index) {
                                            viewModel.loadNextCommentsPage()
                                        }
                                    }
                                }
                            }

                            // Show progress bar at the bottom when loading next comments page
                            if (viewModel.isLoadingMoreComments) {
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
                            
                            // Bottom padding to avoid input overlap
                            item {
                                Spacer(modifier = Modifier.height(64.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    PostOptionsDialog(
        state = optionsState,
        onDismiss = { optionsState = PostOptionsState.CLOSED },
        onEditClick = {
            optionsState = PostOptionsState.CLOSED
            onNavigateToEditPost(postId)
        },
        onDeleteConfirmClick = {
            if (optionsState == PostOptionsState.OPTIONS) {
                optionsState = PostOptionsState.CONFIRM_DELETE
            } else if (optionsState == PostOptionsState.CONFIRM_DELETE) {
                viewModel.deletePost(
                    onSuccess = {
                        optionsState = PostOptionsState.SUCCESS_DELETE
                    },
                    onError = { errorMsg ->
                        android.widget.Toast.makeText(context, errorMsg, android.widget.Toast.LENGTH_SHORT).show()
                        optionsState = PostOptionsState.CLOSED
                    }
                )
            }
        },
        onSuccessDismiss = {
            optionsState = PostOptionsState.CLOSED
            onBack()
        }
    )
}

object CommentDraftManager {
    private val drafts = mutableMapOf<Int, String>()

    fun saveDraft(postId: Int, text: String) {
        if (text.isEmpty()) {
            drafts.remove(postId)
        } else {
            drafts[postId] = text
        }
    }

    fun getDraft(postId: Int): String = drafts[postId] ?: ""

    fun clearDraft(postId: Int) {
        drafts.remove(postId)
    }
}

enum class PostOptionsState {
    CLOSED,
    OPTIONS,
    CONFIRM_DELETE,
    SUCCESS_DELETE
}

@Composable
fun PostOptionsDialog(
    state: PostOptionsState,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteConfirmClick: () -> Unit,
    onSuccessDismiss: () -> Unit
) {
    if (state == PostOptionsState.CLOSED) return

    Dialog(onDismissRequest = onDismiss) {
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
                when (state) {
                    PostOptionsState.OPTIONS -> {
                        DialogActionRow(
                            text = "Eliminar",
                            textColor = Color(0xFFC62828),
                            onClick = onDeleteConfirmClick
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
                        DialogActionRow(
                            text = "Editar",
                            textColor = Color(0xFFC62828),
                            onClick = onEditClick
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
                        DialogActionRow(
                            text = "Cancelar",
                            textColor = Color.Black,
                            onClick = onDismiss
                        )
                    }
                    PostOptionsState.CONFIRM_DELETE -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Confirmar eliminación",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Al borrar la publicación no se podrá recuperar",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
                        DialogActionRow(
                            text = "Eliminar",
                            textColor = Color(0xFFC62828),
                            onClick = onDeleteConfirmClick
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
                        DialogActionRow(
                            text = "Cancelar",
                            textColor = Color.Black,
                            onClick = onDismiss
                        )
                    }
                    PostOptionsState.SUCCESS_DELETE -> {
                        DialogActionRow(
                            text = "Eliminado con éxito",
                            textColor = Color.Black,
                            onClick = onSuccessDismiss
                        )
                    }
                    PostOptionsState.CLOSED -> {}
                }
            }
        }
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
