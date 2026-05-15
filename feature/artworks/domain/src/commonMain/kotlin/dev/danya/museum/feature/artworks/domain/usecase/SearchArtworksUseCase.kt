package dev.danya.museum.feature.artworks.domain.usecase

import dev.danya.museum.core.common.result.Result
import dev.danya.museum.feature.artworks.domain.entity.ArtworkSummary
import dev.danya.museum.feature.artworks.domain.repository.ArtworkRepository

class SearchArtworksUseCase(private val repository: ArtworkRepository) {
    suspend operator fun invoke(
        query: String,
        departmentId: Int? = null,
        artistOrCulture: Boolean = false,
        hasImages: Boolean = true,
    ): Result<List<ArtworkSummary>> {
        if (query.isBlank()) return Result.Success(emptyList())
        return repository.searchArtworks(query.trim(), departmentId, artistOrCulture, hasImages)
    }
}
