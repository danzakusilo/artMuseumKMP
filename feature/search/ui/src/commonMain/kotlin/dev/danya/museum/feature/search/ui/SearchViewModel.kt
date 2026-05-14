package dev.danya.museum.feature.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.danya.museum.core.common.result.Result
import dev.danya.museum.feature.artworks.domain.entity.Department
import dev.danya.museum.feature.artworks.domain.usecase.SearchArtworksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchArtworks: SearchArtworksUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
    }

    fun onDepartmentSelected(department: Department?) {
        _state.update { it.copy(selectedDepartment = department) }
    }

    fun onSearch() {
        val current = _state.value
        if (current.query.isBlank()) return
        _state.update { it.copy(resultState = ResultState.Loading) }
        viewModelScope.launch {
            val result = searchArtworks(
                query = current.query,
                departmentId = current.selectedDepartment?.id,
            )
            _state.update {
                it.copy(
                    resultState = when (result) {
                        is Result.Success -> ResultState.Content(result.data)
                        is Result.Error -> ResultState.Error(result.error)
                    },
                )
            }
        }
    }
}
