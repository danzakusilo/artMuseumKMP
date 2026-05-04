package dev.danya.museum.feature.homescreen.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.danya.museum.core.common.result.Result
import dev.danya.museum.feature.artworks.domain.usecase.SearchArtworksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeScreenViewModel(private val searchArtworks: SearchArtworksUseCase) : ViewModel() {

    private val _state = MutableStateFlow(HomeScreenState())
    val state: StateFlow<HomeScreenState> = _state.asStateFlow()

    fun onQueryChange(query: String) {
        _state.value = _state.value.copy(query = query)
    }

    fun search() {
        val query = _state.value.query
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, result = "")
            _state.value = when (val result = searchArtworks(query)) {
                is Result.Success -> _state.value.copy(
                    isLoading = false,
                    result = result.data.joinToString("\n\n"),
                )
                is Result.Error -> _state.value.copy(
                    isLoading = false,
                    result = "Error: ${result.error}",
                )
            }
        }
    }
}