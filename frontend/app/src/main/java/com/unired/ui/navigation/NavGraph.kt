package com.unired.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import androidx.navigation.compose.NavHost
import com.unired.ui.auth.LoginScreen
import com.unired.ui.auth.RegisterScreen
import com.unired.ui.feed.FeedScreen
import com.unired.util.SessionManager

@Composable
fun NavGraph() {

    val navController = rememberNavController()
    val startDest = if (SessionManager.isLoggedIn()) Screen.Feed.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {

        composable(Screen.Login.route) {

            LoginScreen(

                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },

                onLoginSuccess = {
                    navController.navigate(Screen.Feed.route)
                }
            )
        }

        composable(Screen.Register.route) {

            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Feed.route)
                },
                onNavigateToRegister = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Feed.route) {

            FeedScreen()
        }

        composable(Screen.Friends.route) {
            com.unired.ui.friends.FriendsScreen(
                onNavigateToFeed = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Feed.route) { inclusive = false }
                    }
                },
                onNavigateToProfile = { userId ->
                    navController.navigate("profile/$userId")
                },
                onNavigateToCreatePost = {
                    navController.navigate("create_post")
                },
                onNavigateToProfileSelf = {
                    val currentUserId = SessionManager.getUserId()
                    navController.navigate("profile/$currentUserId")
                }
            )
        }
    }
}