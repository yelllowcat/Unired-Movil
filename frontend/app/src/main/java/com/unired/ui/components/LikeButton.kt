package com.unired.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unired.R

@Composable
fun LikeButton(
    hasLiked: Boolean,
    likesCount: Int,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    fontSize: TextUnit = 14.sp,
    useCustomPostIcons: Boolean = false,
    textColor: Color = Color.DarkGray
) {
    // Spring-based scale animation on click
    val scale by animateFloatAsState(
        targetValue = if (hasLiked) 1.25f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "likeButtonScale"
    )

    // Color transition animation
    val iconColor by animateColorAsState(
        targetValue = if (hasLiked) Color(0xFFE91E63) else Color.Gray, // Vibrant pink-red
        label = "likeButtonColor"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // No ripple for clean animation
                onClick = onLikeClick
            )
            .padding(vertical = 4.dp)
    ) {
        if (useCustomPostIcons) {
            Image(
                painter = painterResource(id = if (hasLiked) R.drawable.ic_heart_like else R.drawable.ic_heart),
                contentDescription = "Me gusta",
                modifier = Modifier
                    .size(iconSize)
                    .scale(scale)
            )
        } else {
            Icon(
                imageVector = if (hasLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Me gusta",
                tint = iconColor,
                modifier = Modifier
                    .size(iconSize)
                    .scale(scale)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = likesCount.toString(),
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}
