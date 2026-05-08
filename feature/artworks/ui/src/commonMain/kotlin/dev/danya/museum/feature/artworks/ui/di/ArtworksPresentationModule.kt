package dev.danya.museum.feature.artworks.ui.di

import dev.danya.museum.feature.artworks.domain.usecase.GetArtworkFeedUseCase
import dev.danya.museum.feature.artworks.domain.usecase.ToggleFavoriteUseCase
import dev.danya.museum.feature.artworks.ui.feed.SwipeFeedViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val artworksPresentationModule = module {
    factory { GetArtworkFeedUseCase(get()) }
    factory { ToggleFavoriteUseCase(get()) }
    viewModel { SwipeFeedViewModel(get(), get()) }
}
