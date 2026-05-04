package dev.danya.museum.feature.artworks.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.danya.museum.core.common.dispatchers.AppDispatchers
import dev.danya.museum.core.database.MuseumDatabase
import dev.danya.museum.feature.artworks.domain.entity.Exhibit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExhibitLocalDataSource(
    private val db: MuseumDatabase,
    private val dispatchers: AppDispatchers,
) {
    fun getExhibits(): Flow<List<Exhibit>> =
        db.exhibitQueries.selectAll()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { rows ->
                rows.map { row ->
                    val count = db.exhibitArtworkQueries
                        .countForExhibit(row.id)
                        .executeAsOne()
                    Exhibit(
                        id = row.id,
                        name = row.name,
                        createdAt = row.createdAt,
                        artworkCount = count.toInt(),
                    )
                }
            }

    fun createExhibit(name: String, createdAt: Long): Exhibit {
        db.exhibitQueries.insert(name = name, createdAt = createdAt)
        val id = db.exhibitQueries.lastInsertRowId().executeAsOne()
        return Exhibit(id = id, name = name, createdAt = createdAt, artworkCount = 0)
    }

    fun addArtwork(exhibitId: Long, artworkId: Int, addedAt: Long) {
        db.exhibitArtworkQueries.add(
            exhibitId = exhibitId,
            artworkId = artworkId.toLong(),
            addedAt = addedAt,
        )
    }

    fun removeArtwork(exhibitId: Long, artworkId: Int) {
        db.exhibitArtworkQueries.remove(
            exhibitId = exhibitId,
            artworkId = artworkId.toLong(),
        )
    }
}
