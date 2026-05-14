package dev.danya.museum.feature.artworks.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.danya.museum.core.common.dispatchers.AppDispatchers
import dev.danya.museum.core.database.MuseumDatabase
import dev.danya.museum.feature.artworks.data.mapper.toSummary
import dev.danya.museum.feature.artworks.domain.entity.ArtworkSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ArtworkLocalDataSource(
    private val db: MuseumDatabase,
    private val dispatchers: AppDispatchers,
) {
    fun getFavorites(): Flow<List<ArtworkSummary>> =
        db.artworkQueries.selectFavorites()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { list -> list.map { it.toSummary() } }

    fun isFavorite(id: Int): Boolean =
        db.artworkQueries.isFavorite(id.toLong()).executeAsOneOrNull() ?: false

    fun upsert(
        id: Int,
        title: String,
        primaryImage: String?,
        artistDisplayName: String?,
        objectDate: String?,
        culture: String?,
        period: String?,
        dynasty: String?,
        medium: String?,
        dimensions: String?,
        department: String,
        classification: String?,
        repository: String?,
        cachedAt: Long,
    ) {
        db.artworkQueries.insertOrIgnore(
            id = id.toLong(),
            title = title,
            primaryImage = primaryImage,
            artistDisplayName = artistDisplayName,
            objectDate = objectDate,
            culture = culture,
            period = period,
            dynasty = dynasty,
            medium = medium,
            dimensions = dimensions,
            department = department,
            classification = classification,
            repository = repository,
            cachedAt = cachedAt,
        )
        db.artworkQueries.updateMetadata(
            title = title,
            primaryImage = primaryImage,
            artistDisplayName = artistDisplayName,
            objectDate = objectDate,
            culture = culture,
            period = period,
            dynasty = dynasty,
            medium = medium,
            dimensions = dimensions,
            department = department,
            classification = classification,
            repository = repository,
            cachedAt = cachedAt,
            id = id.toLong(),
        )
    }

    fun setFavorite(id: Int, isFavorite: Boolean, favoritedAt: Long?) {
        db.artworkQueries.setFavorite(
            isFavorite = isFavorite,
            favoritedAt = favoritedAt,
            id = id.toLong(),
        )
    }

    fun deleteIfOrphan(id: Int) {
        db.artworkQueries.deleteIfOrphan(id.toLong())
    }

    fun evictLooseCache(maxKeep: Int) {
        db.artworkQueries.evictLooseCache(maxKeep.toLong())
    }
}
