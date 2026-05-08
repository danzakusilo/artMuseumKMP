package dev.danya.museum.feature.artworks.ui.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.danya.museum.feature.artworks.ui.feed.SwipeFeedScreen

fun NavGraphBuilder.feedGraph() {
    composable<FeedRoute> { SwipeFeedScreen() }
}
