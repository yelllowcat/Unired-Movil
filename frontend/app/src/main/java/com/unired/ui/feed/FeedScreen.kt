package com.unired.ui.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.unired.R // Importa tu R para los drawables
import com.unired.data.model.Post

@Composable
fun FeedScreen() {
    // Datos de ejemplo
    val samplePosts = remember {
        listOf(
            Post(
                id = 1,
                userName = "María García",
                userPhotoRes = R.drawable.ic_profile,
                date = "Hace 2 horas",
                postImageRes = R.drawable.ic_friends,
                description = "¡Increíble día en la universidad!",
                likesCount = 24,
                commentsCount = 5
            ),

            Post(
                id = 2,
                userName = "Carlos López",
                userPhotoRes = R.drawable.ic_profile,
                date = "Hace 5 horas",
                postImageRes = null,
                description = "Compartiendo mis apuntes de cálculo. Espero les sirvan.",
                likesCount = 15,
                commentsCount = 3
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ){
        Image(
            painter = painterResource(id = R.drawable.fondo_unired),
            contentDescription = "Fondo del feed",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)

        ) {
            items(samplePosts) { post ->
                PostCard(post = post)
            }
        }

    }


}