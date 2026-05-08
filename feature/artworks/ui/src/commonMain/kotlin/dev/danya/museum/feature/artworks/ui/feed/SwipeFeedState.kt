package dev.danya.museum.feature.artworks.ui.feed

import dev.danya.museum.feature.artworks.domain.entity.Artwork

sealed class SwipeFeedState {
    data object Loading : SwipeFeedState()
    data class Content(
        val artworks: List<Artwork>,
        val favorites: Set<Int>,
        val isLoadingMore: Boolean,
    ) : SwipeFeedState()
    data class Error(val message: String) : SwipeFeedState()
}
