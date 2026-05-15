package dev.danya.museum.feature.artworks.ui.feed

import dev.danya.museum.core.common.result.AppError
import dev.danya.museum.feature.artworks.domain.entity.Artwork
import dev.danya.museum.feature.artworks.domain.entity.Department

sealed class SwipeFeedState {
    data object Loading : SwipeFeedState()
    data class Content(
        val artworks: List<Artwork>,
        val favorites: Set<Int>,
        val isLoadingMore: Boolean,
        val currentDepartment: Department,
    ) : SwipeFeedState()
    data class Error(val error: AppError) : SwipeFeedState()
}
