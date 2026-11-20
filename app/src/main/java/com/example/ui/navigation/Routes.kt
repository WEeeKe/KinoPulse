package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object SearchRoute

@Serializable
object FavoritesRoute

@Serializable
data class ChatRoute(val movieId: String = "global")

@Serializable
object SettingsRoute

@Serializable
data class MovieDetailRoute(val movieId: String)

@Serializable
data class PlayerRoute(val movieId: String)
