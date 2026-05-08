package dev.danya.museum.feature.artworks.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.danya.museum.core.common.result.Result
import dev.danya.museum.feature.artworks.domain.usecase.GetArtworkFeedUseCase
import dev.danya.museum.feature.artworks.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val PREFETCH_THRESHOLD = 3

class SwipeFeedViewModel(
    private val getArtworkFeed: GetArtworkFeedUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<SwipeFeedState>(SwipeFeedState.Loading)
    val state: StateFlow<SwipeFeedState> = _state.asStateFlow()

    private val favorites = mutableSetOf<Int>()

    init {
        loadInitialPage()
    }

    private fun loadInitialPage() {
        viewModelScope.launch {
            _state.value = SwipeFeedState.Loading
            when (val result = getArtworkFeed()) {
                is Result.Success -> _state.value = SwipeFeedState.Content(
                    artworks = result.data,
                    favorites = favorites.toSet(),
                    isLoadingMore = false,
                )

                is Result.Error -> _state.value = SwipeFeedState.Error(
                    result.error.toString(),
                )
            }
        }
    }

    fun onPageSettled(index: Int) {
        val current = _state.value as? SwipeFeedState.Content ?: return
        if (current.isLoadingMore) return
        if (index >= current.artworks.size - PREFETCH_THRESHOLD) {
            loadMore()
        }
    }

    private fun loadMore() {
        val current = _state.value as? SwipeFeedState.Content ?: return
        _state.value = current.copy(isLoadingMore = true)
        viewModelScope.launch {
            when (val result = getArtworkFeed()) {
                is Result.Success -> {
                    val updated = current.artworks + result.data
                    _state.value = SwipeFeedState.Content(
                        artworks = updated,
                        favorites = favorites.toSet(),
                        isLoadingMore = false,
                    )
                }

                is Result.Error -> {
                    _state.value = current.copy(isLoadingMore = false)
                }
            }
        }
    }

    fun onToggleFavorite(artworkId: Int) {
        viewModelScope.launch {
            when (toggleFavorite(artworkId)) {
                is Result.Success -> {
                    if (!favorites.add(artworkId)) favorites.remove(artworkId)
                    val current = _state.value as? SwipeFeedState.Content ?: return@launch
                    _state.value = current.copy(favorites = favorites.toSet())
                }

                is Result.Error -> {}
            }
        }
    }

    fun retry() {
        loadInitialPage()
    }
}
