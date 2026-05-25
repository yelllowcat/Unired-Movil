package com.unired

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.unired.data.repository.NotificationRepository
import com.unired.ui.components.BottomNavItem
import com.unired.ui.components.UniRedBottomBar
import com.unired.ui.navigation.NavGraph
import com.unired.ui.navigation.Screen
import com.unired.util.SessionManager

@Composable
fun UniRedApp() {

    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val hideBaraIn = listOf(
        Screen.Login.route,
        Screen.Register.route,
        Screen.PostDetail.route,
        Screen.EditProfile.route
    )
    val showBottomBar = currentRoute !in hideBaraIn

    val notificationRepository = remember { NotificationRepository() }
    var unreadCount by remember { mutableStateOf(0) }

    LaunchedEffect(currentRoute) {
        if (SessionManager.isLoggedIn()) {
            try {
                unreadCount = notificationRepository.getUnreadCount()
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            if (SessionManager.isLoggedIn()) {
                try {
                    unreadCount = notificationRepository.getUnreadCount()
                } catch (_: Exception) {}
            }
            kotlinx.coroutines.delay(15_000)
        }
    }

    val bottomItems = listOf(
        BottomNavItem("Feed", Screen.Feed.route, R.drawable.ic_feed),
        BottomNavItem("newPost", Screen.CreatePost.route, R.drawable.ic_new_post),
        BottomNavItem("Friends", Screen.Friends.route, R.drawable.ic_friends),
        BottomNavItem("Notifications", Screen.Notifications.route, R.drawable.ic_notifications, unreadCount),
        BottomNavItem("Profile", "profile/me", R.drawable.ic_profile)
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar){
                UniRedBottomBar(
                    items = bottomItems,
                    currentRoute = currentRoute,
                    onItemClick = { item ->
                        if ( currentRoute != item.route){
                            navController.navigate(item.route){
                                popUpTo(Screen.Feed.route) {saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }

}