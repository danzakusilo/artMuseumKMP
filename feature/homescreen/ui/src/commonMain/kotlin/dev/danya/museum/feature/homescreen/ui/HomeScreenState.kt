package dev.danya.museum.feature.homescreen.ui

import dev.danya.museum.feature.artworks.domain.entity.ArtworkSummary

data class HomeScreenState(
    val query: String = "",
    val isLoading: Boolean = false,
    val result: String = "",
    val recentArtworks: List<ArtworkSummary> = emptyList(),
    val isLoadingRecents: Boolean = false,
    val recentsError: Boolean = false,
)