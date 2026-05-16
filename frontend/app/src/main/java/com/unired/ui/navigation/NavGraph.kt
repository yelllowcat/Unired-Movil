package com.unired.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import androidx.navigation.compose.NavHost
import com.unired.ui.auth.LoginScreen
import com.unired.ui.auth.RegisterScreen
import com.unired.ui.feed.FeedScreen

@Composable
fun NavGraph() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
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
    }
}