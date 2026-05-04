package dev.danya.museum.feature.artworks.data.repository

import dev.danya.museum.core.common.result.AppError
import dev.danya.museum.core.common.result.Result
import dev.danya.museum.feature.artworks.data.local.ArtworkLocalDataSource
import dev.danya.museum.feature.artworks.data.mapper.toDomain
import dev.danya.museum.feature.artworks.data.mapper.toSummary
import dev.danya.museum.feature.artworks.data.remote.ArtworkApiService
import dev.danya.museum.feature.artworks.domain.entity.Artwork
import dev.danya.museum.feature.artworks.domain.entity.ArtworkSummary
import dev.danya.museum.feature.artworks.domain.repository.ArtworkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class ArtworkRepositoryImpl(
    private val api: ArtworkApiService,
    private val local: ArtworkLocalDataSource,
) : ArtworkRepository {

    override suspend fun searchArtworks(query: String): Result<List<ArtworkSummary>> =
        runCatching { api.searchAndFetch(query).map { it.toSummary() } }
            .toResult()

    override suspend fun getArtworkDetail(id: Int): Result<Artwork> =
        runCatching {
            val dto = api.getObject(id)
            local.upsert(
                id = dto.objectID,
                title = dto.title,
                primaryImage = dto.primaryImageSmall?.takeIf { it.isNotBlank() }
                    ?: dto.primaryImage?.takeIf { it.isNotBlank() },
                artistDisplayName = dto.artistDisplayName?.takeIf { it.isNotBlank() },
                objectDate = dto.objectDate?.takeIf { it.isNotBlank() },
                culture = dto.culture?.takeIf { it.isNotBlank() },
                period = dto.period?.takeIf { it.isNotBlank() },
                dynasty = dto.dynasty?.takeIf { it.isNotBlank() },
                medium = dto.medium?.takeIf { it.isNotBlank() },
                dimensions = dto.dimensions?.takeIf { it.isNotBlank() },
                department = dto.department,
                classification = dto.classification?.takeIf { it.isNotBlank() },
                repository = dto.repository?.takeIf { it.isNotBlank() },
                cachedAt = Clock.System.now().toEpochMilliseconds(),
            )
            dto.toDomain()
        }.toResult()

    override fun getFavorites(): Flow<Result<List<ArtworkSummary>>> =
        local.getFavorites().map { Result.Success(it) }

    override suspend fun toggleFavorite(artworkId: Int): Result<Unit> =
        runCatching {
            val currently = local.isFavorite(artworkId)
            val nowFavorite = !currently
            if (nowFavorite) {
                local.setFavorite(
                    id = artworkId,
                    isFavorite = true,
                    favoritedAt = Clock.System.now().toEpochMilliseconds(),
                )
            } else {
                local.setFavorite(id = artworkId, isFavorite = false, favoritedAt = null)
                local.deleteIfOrphan(artworkId)
            }
        }.toResult()

    private fun <T> kotlin.Result<T>.toResult(): Result<T> =
        fold(
            onSuccess = { Result.Success(it) },
            onFailure = { Result.Error(AppError.NetworkError(null, it.message ?: "Unknown error")) },
        )
}
