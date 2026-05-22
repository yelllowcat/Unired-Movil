package com.unired.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.compose.NavHost
import androidx.navigation.navArgument
import com.unired.ui.auth.LoginScreen
import com.unired.ui.auth.RegisterScreen
import com.unired.ui.feed.FeedScreen
import com.unired.util.SessionManager
import com.unired.ui.post.CreatePostScreen
import com.unired.ui.friends.FriendsScreen
import com.unired.ui.profile.ProfileScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val startDest = if (SessionManager.isLoggedIn()) Screen.Feed.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDest,
        modifier = modifier
    ) {

        composable(Screen.Login.route) {

            LoginScreen(

                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },

                onLoginSuccess = {
                    navController.navigate(Screen.Feed.route){
                        popUpTo(Screen.Login.route){ inclusive = true}
                    }
                }
            )
        }

        composable(Screen.Register.route) {

            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Feed.route) {

            FeedScreen()
        }

        composable(Screen.CreatePost.route) {
            CreatePostScreen()
        }

        composable(Screen.Friends.route) {
            FriendsScreen()
        }

        composable(
            route = Screen.Profile.route,
            arguments = listOf(navArgument("userId") { defaultValue = "me" })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: "me"
            ProfileScreen(userId = userId)
        }
    }


}