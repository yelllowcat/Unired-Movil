package com.unired.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BottomNavItem(
    val label: String,
    val route: String,
    val iconRes: Int,
    val badgeCount: Int = 0
)

@Composable
fun UniRedBottomBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                BottomNavTab(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavTab(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp)
    ) {
        // Línea azul indicadora
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(3.dp)
                .background(if (isSelected) Color(0xFF2196F3) else Color.Transparent)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(contentAlignment = Alignment.TopEnd) {
            Image(
                painter = painterResource(id = item.iconRes),
                contentDescription = item.label,
                modifier = Modifier.size(28.dp)
            )
            if (item.badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = 8.dp, y = (-4).dp)
                        .background(Color.Red, shape = CircleShape)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
