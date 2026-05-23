package com.unired

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.unired.ui.components.BottomNavItem
import com.unired.ui.components.UniRedBottomBar
import com.unired.ui.navigation.NavGraph
import com.unired.ui.navigation.Screen

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

    val bottomItems = listOf(
        BottomNavItem("Feed", Screen.Feed.route, R.drawable.ic_feed),
        BottomNavItem("newPost", Screen.CreatePost.route, R.drawable.ic_new_post),
        BottomNavItem("Friends", Screen.Friends.route, R.drawable.ic_friends),
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