package com.unired.ui.navigation

sealed class Screen(val route: String) {

    object Login : Screen("login")

    object Register : Screen("register")

    object Feed : Screen("feed")

    object CreatePost : Screen("create_post")

    object PostDetail : Screen("post_detail/{postId}")

    object Friends : Screen("friends")

    object Profile : Screen("profile/{userId}")

    object EditProfile : Screen("edit_profile")
}