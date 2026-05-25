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
import com.unired.ui.post.PostDetailScreen
import com.unired.ui.friends.FriendsScreen
import com.unired.ui.profile.ProfileScreen
import com.unired.ui.profile.EditProfileScreen
import com.unired.ui.notifications.NotificationsScreen

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
            FeedScreen(
                onNavigateToPostDetail = { postId ->
                    navController.navigate("post_detail/$postId")
                },
                onNavigateToProfile = { userId ->
                    navController.navigate("profile/$userId")
                }
            )
        }

        composable(
            route = Screen.PostDetail.route,
            arguments = listOf(navArgument("postId") { type = androidx.navigation.NavType.IntType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getInt("postId") ?: 0
            PostDetailScreen(
                postId = postId,
                onNavigateToProfile = { userId ->
                    navController.navigate("profile/$userId")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.CreatePost.route) {
            CreatePostScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPostCreated = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Feed.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Friends.route) {
            FriendsScreen(
                onNavigateToProfile = { userId ->
                    navController.navigate("profile/$userId")
                }
            )
        }

        composable(
            route = Screen.Profile.route,
            arguments = listOf(navArgument("userId") { defaultValue = "me" })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: "me"
            ProfileScreen(
                userId = userId,
                onNavigateToPostDetail = { postId ->
                    navController.navigate("post_detail/$postId")
                },
                onNavigateToProfile = { otherUserId ->
                    navController.navigate("profile/$otherUserId")
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onEditProfileClick = {
                    navController.navigate(Screen.EditProfile.route)
                }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onProfileUpdated = {
                    navController.popBackStack()
                },
                onAccountDeleted = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onNavigateToPostDetail = { postId ->
                    navController.navigate("post_detail/$postId")
                },
                onNavigateToProfile = { userId ->
                    navController.navigate("profile/$userId")
                }
            )
        }
    }
}