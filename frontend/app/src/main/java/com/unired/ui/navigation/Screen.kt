package com.unired.ui.navigation

sealed class Screen(val route: String) {

    object Login : Screen("login")

    object Register : Screen("register")

    object Feed : Screen("feed")

    object Friends : Screen("friends")
}