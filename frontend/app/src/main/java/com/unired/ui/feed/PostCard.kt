package com.unired.ui.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.unired.data.model.Post
import com.unired.R
import com.unired.data.api.ApiClient
import com.unired.ui.components.AvatarImage
import com.unired.ui.components.LikeButton
import com.unired.ui.theme.UniRedBackground
import com.unired.util.DateFormatter

@Composable
fun PostCard(
    post: Post,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onProfileClick: (Int) -> Unit,
    onPostClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val serverUrl = ApiClient.BASE_URL.substringBefore("/api/")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onPostClick(post.postId) },
        shape = RoundedCornerShape(15.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = UniRedBackground)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onProfileClick(post.userId) }
                    .padding(vertical = 4.dp)
            ) {
                AvatarImage(
                    imageUrl = post.authorPicture,
                    fullName = post.authorName,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = post.authorName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = DateFormatter.formatDateString(post.createdAt),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            post.image?.let { imgUrl ->
                val fullUrl = if (imgUrl.startsWith("http")) imgUrl else "$serverUrl$imgUrl"
                AsyncImage(
                    model = fullUrl,
                    contentDescription = "Imagen de la publicación",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = post.content,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LikeButton(
                    hasLiked = post.hasLiked,
                    likesCount = post.likesCount,
                    onLikeClick = onLikeClick,
                    useCustomPostIcons = true
                )

                Spacer(modifier = Modifier.width(24.dp))

                IconButton(onClick = onCommentClick) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_coment),
                        contentDescription = "Comentarios",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = post.commentsCount.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
