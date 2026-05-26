package com.unired.ui.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unired.R
import com.unired.ui.components.EmptyState
import com.unired.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onNavigateToPostDetail: (Int) -> Unit,
    onNavigateToProfile: (Int) -> Unit,
    viewModel: FeedViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState = viewModel.uiState
    val isRefreshing = viewModel.isRefreshing

    LaunchedEffect(Unit) {
        viewModel.refreshFeed()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo_unired),
            contentDescription = "Fondo del feed",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshFeed() },
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = uiState) {
                is FeedUiState.Loading -> {
                    LoadingIndicator(modifier = Modifier.fillMaxSize())
                }
                is FeedUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = Color.Red,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadFeed() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33B5B5))
                        ) {
                            Text(text = "Reintentar", color = Color.White)
                        }
                    }
                }
                is FeedUiState.Success -> {
                    if (state.posts.isEmpty()) {
                        EmptyState(
                            message = "No hay publicaciones aún. ¡Sé el primero en compartir algo!",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            itemsIndexed(state.posts, key = { _, post -> post.postId }) { index, post ->
                                PostCard(
                                    post = post,
                                    onLikeClick = { viewModel.toggleLike(post.postId) },
                                    onCommentClick = { onNavigateToPostDetail(post.postId) },
                                    onProfileClick = { onNavigateToProfile(post.userId) },
                                    onPostClick = { onNavigateToPostDetail(post.postId) }
                                )

                                // Trigger loading next page when close to the bottom
                                if (index >= state.posts.lastIndex - 2) {
                                    LaunchedEffect(index) {
                                        viewModel.loadNextPage()
                                    }
                                }
                            }

                            // Show progress bar at the bottom when loading next page
                            if (viewModel.isLoadingMore) {
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