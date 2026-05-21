package com.unired.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavBar(
    selectedRoute: String,
    onCreatePostClick: () -> Unit,
    onFriendsClick: () -> Unit,
    onFeedClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Column {
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tealColor = Color(0xFF33B5B5) // Teal color from mockup

            // Create Post
            NavItem(
                icon = Icons.Filled.AddCircleOutline,
                isSelected = selectedRoute == "create_post",
                onClick = onCreatePostClick,
                activeColor = tealColor
            )

            // Friends
            NavItem(
                icon = Icons.Outlined.People,
                isSelected = selectedRoute == "friends",
                onClick = onFriendsClick,
                activeColor = tealColor
            )

            // Feed
            NavItem(
                icon = Icons.AutoMirrored.Outlined.ListAlt,
                isSelected = selectedRoute == "feed",
                onClick = onFeedClick,
                activeColor = tealColor
            )

            // Profile
            NavItem(
                icon = Icons.Outlined.Person,
                isSelected = selectedRoute.startsWith("profile"),
                onClick = onProfileClick,
                activeColor = tealColor
            )
        }
    }
}

@Composable
private fun RowScope.NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    activeColor: Color
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            // Selected Indicator at the top of bottom bar
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(3.dp)
                        .background(activeColor)
                )
            } else {
                Spacer(modifier = Modifier.height(3.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) activeColor else Color.Gray,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
