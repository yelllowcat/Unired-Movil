package com.unired.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.unired.data.api.ApiClient

@Composable
fun AvatarImage(
    imageUrl: String?,
    fullName: String,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    fontSize: Int = 16
) {
    val serverUrl = ApiClient.BASE_URL.substringBefore("/api/")
    val fullUrl = when {
        imageUrl == null -> null
        imageUrl.startsWith("http") -> imageUrl
        imageUrl == "default_avatar.png" -> null
        else -> "$serverUrl$imageUrl"
    }

    Box(modifier = modifier.clip(shape)) {
        if (fullUrl != null) {
            AsyncImage(
                model = fullUrl,
                contentDescription = "Avatar de $fullName",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                onError = {
                    // Fallback to initials if load fails
                }
            )
        } else {
            val initials = fullName.split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .map { it.first().uppercase() }
                .joinToString("")

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0xFFE0F2F1)), // Light teal background
                contentAlignment = Alignment.Center
            ) {
                if (initials.isNotEmpty()) {
                    Text(
                        text = initials,
                        color = Color(0xFF00796B), // Dark teal text
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSize.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = Color(0xFF00796B)
                    )
                }
            }
        }
    }
}
