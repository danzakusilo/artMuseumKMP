package dev.danya.museum.navigation

import dev.danya.museum.feature.artworks.ui.nav.FeedRoute
import dev.danya.museum.feature.homescreen.ui.nav.HomeRoute
import kotlinx.serialization.Serializable

@Serializable data object FavoritesRoute

enum class TopLevelDestination(
    val route: Any,
    val label: String,
) {
    HOME(HomeRoute, "Home"),
    DISCOVER(FeedRoute, "Discover"),
    FAVORITES(FavoritesRoute, "Favorites"),
}