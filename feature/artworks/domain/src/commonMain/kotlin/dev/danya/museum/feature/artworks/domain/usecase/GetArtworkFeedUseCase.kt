package dev.danya.museum.feature.artworks.domain.usecase

import dev.danya.museum.core.common.result.Result
import dev.danya.museum.feature.artworks.domain.entity.Artwork
import dev.danya.museum.feature.artworks.domain.repository.ArtworkRepository

private const val DEFAULT_FEED_PAGE_SIZE = 10

class GetArtworkFeedUseCase(private val repository: ArtworkRepository) {
    suspend operator fun invoke(departmentId: Int, limit: Int = DEFAULT_FEED_PAGE_SIZE): Result<List<Artwork>> =
        repository.getArtworkFeedPage(departmentId, limit)
}
