package dev.danya.museum.feature.artworks.data.repository

import dev.danya.museum.core.common.result.AppError
import dev.danya.museum.core.common.result.Result
import dev.danya.museum.feature.artworks.data.local.ArtworkLocalDataSource
import dev.danya.museum.feature.artworks.data.local.ExhibitLocalDataSource
import dev.danya.museum.feature.artworks.data.mapper.toDomain
import dev.danya.museum.feature.artworks.data.mapper.toSummary
import dev.danya.museum.core.database.Artwork as ArtworkEntity
import dev.danya.museum.feature.artworks.data.remote.ArtworkApiService
import dev.danya.museum.feature.artworks.data.remote.dto.ArtworkDetailDto
import dev.danya.museum.feature.artworks.domain.entity.Artwork
import dev.danya.museum.feature.artworks.domain.entity.ArtworkSummary
import dev.danya.museum.feature.artworks.domain.repository.ArtworkRepository
import kotlinx.coroutines.CancellationException
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
    private val exhibitLocal: ExhibitLocalDataSource,
) : ArtworkRepository {

    private val feedMutex = Mutex()
    private val feedIdPool = mutableListOf<Int>()
    private var feedCursor = 0
    private var activeDepartmentId: Int? = null
    private val looseCacheLimit = 200
    private val imagelessOvershoot = 2
    private val minBatchSize = 5

    private val searchMutex = Mutex()
    private val searchIdPool = mutableListOf<Int>()
    private var searchCursor = 0
    private var activeSearchKey: SearchKey? = null

    private data class SearchKey(
        val query: String,
        val departmentId: Int?,
        val artistOrCulture: Boolean,
        val hasImages: Boolean,
    )

    override suspend fun searchArtworks(
        query: String,
        departmentId: Int?,
        artistOrCulture: Boolean,
        hasImages: Boolean,
    ): Result<List<ArtworkSummary>> = searchMutex.withLock {
        try {
            val key = SearchKey(query, departmentId, artistOrCulture, hasImages)
            if (key != activeSearchKey) {
                val ids = api.search(query, departmentId, artistOrCulture, hasImages)
                    .objectIDs?.take(SEARCH_ID_LIMIT) ?: emptyList()
                searchIdPool.clear()
                searchIdPool.addAll(ids)
                activeSearchKey = key
            }
            searchCursor = 0
            fetchSearchPage()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.toAppError())
        }
    }

    override suspend fun loadMoreSearchResults(): Result<List<ArtworkSummary>> =
        searchMutex.withLock {
            try {
                fetchSearchPage()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.Error(e.toAppError())
            }
        }

    private suspend fun fetchSearchPage(): Result<List<ArtworkSummary>> {
        if (searchCursor >= searchIdPool.size) return Result.Success(emptyList())
        val end = minOf(searchCursor + SEARCH_PAGE_SIZE, searchIdPool.size)
        val batchIds = searchIdPool.subList(searchCursor, end).toList()
        searchCursor = end
        val entities = resolveArtworks(batchIds)
        return Result.Success(entities.map { it.toSummary() })
    }

    override suspend fun getRecentArtworks(): Result<List<Artwork>> =
        suspendResult {
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
        }

    override suspend fun getArtworkDetail(id: Int): Result<Artwork> =
        suspendResult {
            local.getById(id)?.toDomain() ?: run {
                val dto = api.getObjectSingle(id)
                upsertDto(dto)
                dto.toDomain()
            }
        }

    override suspend fun getArtworkFeedPage(departmentId: Int, limit: Int): Result<List<Artwork>> =
        feedMutex.withLock {
            try {
                if (departmentId != activeDepartmentId) {
                    feedIdPool.clear()
                    feedCursor = 0
                    activeDepartmentId = departmentId
                }
                if (feedIdPool.isEmpty()) {
                    val ids = api.fetchDepartmentObjectIds(departmentId)
                    feedIdPool.addAll(ids.shuffled(Random(Clock.System.now().toEpochMilliseconds())))
                }
                val result = mutableListOf<Artwork>()
                while (result.size < limit && feedCursor < feedIdPool.size) {
                    val remaining = limit - result.size
                    val batchSize = (remaining * imagelessOvershoot).coerceAtLeast(minBatchSize)
                    val end = minOf(feedCursor + batchSize, feedIdPool.size)
                    val batchIds = feedIdPool.subList(feedCursor, end).toList()
                    feedCursor = end
                    val entities = resolveArtworks(batchIds)
                    for (entity in entities) {
                        if (entity.primaryImage == null) continue
                        result.add(entity.toDomain())
                        if (result.size >= limit) break
                    }
                }
                local.evictLooseCache(looseCacheLimit)
                Result.Success(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.Error(e.toAppError())
            }
        }

    override fun getFavorites(): Flow<Result<List<ArtworkSummary>>> =
        local.getFavorites().map { Result.Success(it) }

    override suspend fun isFavorite(artworkId: Int): Result<Boolean> =
        suspendResult { local.isFavorite(artworkId) }

    override suspend fun toggleFavorite(artworkId: Int): Result<Unit> =
        suspendResult {
            val currently = local.isFavorite(artworkId)
            val nowFavorite = !currently
            if (nowFavorite) {
                local.setFavorite(
                    id = artworkId,
                    isFavorite = true,
                    favoritedAt = Clock.System.now().toEpochMilliseconds(),
                )
            } else {
                exhibitLocal.removeAllForArtwork(artworkId)
                local.setFavorite(id = artworkId, isFavorite = false, favoritedAt = null)
                local.deleteIfOrphan(artworkId)
            }
        }

    private suspend fun resolveArtworks(ids: List<Int>): List<ArtworkEntity> {
        val resolved = mutableMapOf<Int, ArtworkEntity>()
        val missingIds = mutableListOf<Int>()
        for (id in ids) {
            val entity = local.getById(id)
            if (entity != null) {
                resolved[id] = entity
            } else {
                missingIds.add(id)
            }
        }
        if (missingIds.isNotEmpty()) {
            val fetched = api.fetchArtworkDetails(missingIds)
            for (dto in fetched) {
                upsertDto(dto)
                local.getById(dto.objectID)?.let { resolved[dto.objectID] = it }
            }
        }
        return ids.mapNotNull { resolved[it] }
    }

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

    private suspend fun <T> suspendResult(block: suspend () -> T): Result<T> =
        try {
            Result.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.toAppError())
        }

    private fun Exception.toAppError(): AppError =
        when (this) {
            is IOException -> AppError.NoInternetError
            else -> AppError.NetworkError(null, message ?: "Unknown error")
        }

    private companion object {
        const val SEARCH_PAGE_SIZE = 10
        const val SEARCH_ID_LIMIT = 80
    }
}
