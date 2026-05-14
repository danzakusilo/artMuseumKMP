package dev.danya.museum.feature.search.ui

import dev.danya.museum.core.common.result.AppError
import dev.danya.museum.feature.artworks.domain.entity.ArtworkSummary
import dev.danya.museum.feature.artworks.domain.entity.Department

data class SearchState(
    val query: String = "",
    val selectedDepartment: Department? = null,
    val resultState: ResultState = ResultState.Idle,
)

sealed class ResultState {
    data object Idle : ResultState()
    data object Loading : ResultState()
    data class Content(val results: List<ArtworkSummary>) : ResultState()
    data class Error(val error: AppError) : ResultState()
}
