package dev.danya.museum.feature.homescreen.ui.di

import dev.danya.museum.feature.artworks.domain.usecase.SearchArtworksUseCase
import dev.danya.museum.feature.homescreen.ui.HomeScreenViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homescreenPresentationModule = module {
    factory { SearchArtworksUseCase(get()) }
    viewModel { HomeScreenViewModel(get()) }
}