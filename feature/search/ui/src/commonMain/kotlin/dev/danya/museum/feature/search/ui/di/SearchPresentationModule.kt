package dev.danya.museum.feature.search.ui.di

import dev.danya.museum.feature.artworks.domain.usecase.LoadMoreSearchResultsUseCase
import dev.danya.museum.feature.artworks.domain.usecase.SearchArtworksUseCase
import dev.danya.museum.feature.search.ui.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val searchPresentationModule = module {
    factory { SearchArtworksUseCase(get()) }
    factory { LoadMoreSearchResultsUseCase(get()) }
    viewModel { SearchViewModel(get(), get()) }
}
