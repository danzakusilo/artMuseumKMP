package dev.danya.museum.feature.artworks.domain.repository

import dev.danya.museum.core.common.result.Result
import dev.danya.museum.feature.artworks.domain.entity.Artwork
import dev.danya.museum.feature.artworks.domain.entity.ArtworkSummary
import kotlinx.coroutines.flow.Flow

interface ArtworkRepository {
    suspend fun searchArtworks(
        query: String,
        departmentId: Int? = null,
        artistOrCulture: Boolean = false,
        hasImages: Boolean = true,
    ): Result<List<ArtworkSummary>>
    suspend fun loadMoreSearchResults(): Result<List<ArtworkSummary>>
    suspend fun getArtworkDetail(id: Int): Result<Artwork>
    suspend fun getRecentArtworks(): Result<List<Artwork>>
    suspend fun getArtworkFeedPage(departmentId: Int, limit: Int): Result<List<Artwork>>
    fun getFavorites(): Flow<Result<List<ArtworkSummary>>>
    suspend fun isFavorite(artworkId: Int): Result<Boolean>
    suspend fun toggleFavorite(artworkId: Int): Result<Unit>
}
