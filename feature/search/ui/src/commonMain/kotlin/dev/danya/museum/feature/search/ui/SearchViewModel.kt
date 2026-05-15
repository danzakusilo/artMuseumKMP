package dev.danya.museum.feature.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.danya.museum.core.common.result.Result
import dev.danya.museum.feature.artworks.domain.entity.Department
import dev.danya.museum.feature.artworks.domain.usecase.LoadMoreSearchResultsUseCase
import dev.danya.museum.feature.artworks.domain.usecase.SearchArtworksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 10

class SearchViewModel(
    private val searchArtworks: SearchArtworksUseCase,
    private val loadMoreSearchResults: LoadMoreSearchResultsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
    }

    fun onDepartmentSelected(department: Department?) {
        _state.update { it.copy(selectedDepartment = department) }
    }

    fun onHasImagesToggled() {
        _state.update { it.copy(hasImages = !it.hasImages) }
    }

    fun onArtistOrCultureToggled() {
        _state.update { it.copy(artistOrCulture = !it.artistOrCulture) }
    }

    fun onSearch() {
        val current = _state.value
        if (current.query.isBlank()) return
        _state.update { it.copy(resultState = ResultState.Loading) }
        viewModelScope.launch {
            val result = searchArtworks(
                query = current.query,
                departmentId = current.selectedDepartment?.id,
                artistOrCulture = current.artistOrCulture,
                hasImages = current.hasImages,
            )
            _state.update {
                it.copy(
                    resultState = when (result) {
                        is Result.Success -> ResultState.Content(
                            results = result.data,
                            hasMore = result.data.size >= PAGE_SIZE,
                        )
                        is Result.Error -> ResultState.Error(result.error)
                    },
                )
            }
        }
    }

    fun onLoadMore() {
        val current = _state.value.resultState as? ResultState.Content ?: return
        if (current.isLoadingMore || !current.hasMore) return
        _state.update { it.copy(resultState = current.copy(isLoadingMore = true)) }
        viewModelScope.launch {
            when (val result = loadMoreSearchResults()) {
                is Result.Success -> {
                    val combined = current.results + result.data
                    _state.update {
                        it.copy(
                            resultState = ResultState.Content(
                                results = combined,
                                hasMore = result.data.size >= PAGE_SIZE,
                                isLoadingMore = false,
                            ),
                        )
                    }
                }
                is Result.Error -> {
                    _state.update { it.copy(resultState = current.copy(isLoadingMore = false)) }
                }
            }
        }
    }
}
