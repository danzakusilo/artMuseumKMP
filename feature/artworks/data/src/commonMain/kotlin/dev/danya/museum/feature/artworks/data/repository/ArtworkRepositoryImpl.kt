package dev.danya.museum.feature.artworks.data.repository

import dev.danya.museum.core.common.result.AppError
import dev.danya.museum.core.common.result.Result
import dev.danya.museum.feature.artworks.data.local.ArtworkLocalDataSource
import dev.danya.museum.feature.artworks.data.mapper.toDomain
import dev.danya.museum.feature.artworks.data.mapper.toSummary
import dev.danya.museum.feature.artworks.data.remote.ArtworkApiService
import dev.danya.museum.feature.artworks.data.remote.dto.ArtworkDetailDto
import dev.danya.museum.feature.artworks.domain.entity.Artwork
import dev.danya.museum.feature.artworks.domain.entity.Department
import dev.danya.museum.feature.artworks.domain.entity.ArtworkSummary
import dev.danya.museum.feature.artworks.domain.repository.ArtworkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

class ArtworkRepositoryImpl(
    private val api: ArtworkApiService,
    private val local: ArtworkLocalDataSource,
) : ArtworkRepository {

    private val feedMutex = Mutex()
    private val feedIdPool = mutableListOf<Int>()
    private var feedCursor = 0
    private val looseCacheLimit = 200

    override suspend fun searchArtworks(
        query: String,
        departmentId: Int?,
        artistOrCulture: Boolean,
    ): Result<List<ArtworkSummary>> =
        runCatching { api.searchAndFetch(query, departmentId, artistOrCulture).map { it.toSummary() } }
            .toResult()

    override suspend fun getRecentArtworks(): Result<List<Artwork>> =
        runCatching {
            val cutoff = Clock.System.now()
                .minus(3.days)
                .toLocalDateTime(TimeZone.UTC)
                .date
                .toString()
            api.fetchRecentArtworks(cutoff)
                .filter { dto ->
                    !dto.primaryImageSmall.isNullOrBlank() || !dto.primaryImage.isNullOrBlank()
                }
                .also { dtos -> dtos.forEach { upsertDto(it) } }
                .map { it.toDomain() }
        }.toResult()

    override suspend fun getArtworkDetail(id: Int): Result<Artwork> =
        runCatching {
            local.getById(id)?.toDomain() ?: run {
                val dto = api.getObjectSingle(id)
                upsertDto(dto)
                dto.toDomain()
            }
        }.toResult()

    override suspend fun getArtworkFeedPage(limit: Int): Result<List<Artwork>> =
        feedMutex.withLock {
            runCatching {
                if (feedIdPool.isEmpty()) {
                    val departmentId = Department.entries.random().id
                    val ids = api.fetchDepartmentObjectIds(departmentId)
                    feedIdPool.addAll(ids.shuffled(Random(Clock.System.now().toEpochMilliseconds())))
                }
                val result = mutableListOf<Artwork>()
                while (result.size < limit && feedCursor < feedIdPool.size) {
                    val end = minOf(feedCursor + limit, feedIdPool.size)
                    val batchIds = feedIdPool.subList(feedCursor, end)
                    feedCursor = end
                    val dtos = api.fetchArtworkDetails(batchIds)
                    for (dto in dtos) {
                        val hasImage = !dto.primaryImageSmall.isNullOrBlank() ||
                            !dto.primaryImage.isNullOrBlank()
                        if (!hasImage) continue
                        upsertDto(dto)
                        result.add(dto.toDomain())
                        if (result.size >= limit) break
                    }
                }
                local.evictLooseCache(looseCacheLimit)
                result
            }.toResult()
        }

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

    private fun upsertDto(dto: ArtworkDetailDto) {
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
    }

    private fun <T> kotlin.Result<T>.toResult(): Result<T> =
        fold(
            onSuccess = { Result.Success(it) },
            onFailure = { e ->
                Result.Error(
                    when (e) {
                        is IOException -> AppError.NoInternetError
                        else -> AppError.NetworkError(null, e.message ?: "Unknown error")
                    },
                )
            },
        )
}
