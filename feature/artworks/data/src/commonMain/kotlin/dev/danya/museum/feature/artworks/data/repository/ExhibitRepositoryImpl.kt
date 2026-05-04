package dev.danya.museum.feature.artworks.data.repository

import dev.danya.museum.core.common.result.AppError
import dev.danya.museum.core.common.result.Result
import dev.danya.museum.feature.artworks.data.local.ExhibitLocalDataSource
import dev.danya.museum.feature.artworks.domain.entity.Exhibit
import dev.danya.museum.feature.artworks.domain.repository.ExhibitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class ExhibitRepositoryImpl(
    private val local: ExhibitLocalDataSource,
) : ExhibitRepository {

    override fun getExhibits(): Flow<Result<List<Exhibit>>> =
        local.getExhibits().map { Result.Success(it) }

    override suspend fun createExhibit(name: String): Result<Exhibit> =
        runCatching {
            local.createExhibit(name, Clock.System.now().toEpochMilliseconds())
        }.toResult()

    override suspend fun addArtworkToExhibit(exhibitId: Long, artworkId: Int): Result<Unit> =
        runCatching {
            local.addArtwork(exhibitId, artworkId, Clock.System.now().toEpochMilliseconds())
        }.toResult()

    override suspend fun removeArtworkFromExhibit(exhibitId: Long, artworkId: Int): Result<Unit> =
        runCatching {
            local.removeArtwork(exhibitId, artworkId)
        }.toResult()

    private fun <T> kotlin.Result<T>.toResult(): Result<T> =
        fold(
            onSuccess = { Result.Success(it) },
            onFailure = { Result.Error(AppError.DatabaseError(it)) },
        )
}
