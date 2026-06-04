package com.unired.ui.navigation

sealed class Screen(val route: String) {

    object Login : Screen("login")

    object Register : Screen("register")

    object Feed : Screen("feed")

    object CreatePost : Screen("create_post")

    object EditPost : Screen("edit_post/{postId}")

    object PostDetail : Screen("post_detail/{postId}")

    object Friends : Screen("friends")

    object Notifications : Screen("notifications")

    object Profile : Screen("profile/{userId}")

    object EditProfile : Screen("edit_profile")
}