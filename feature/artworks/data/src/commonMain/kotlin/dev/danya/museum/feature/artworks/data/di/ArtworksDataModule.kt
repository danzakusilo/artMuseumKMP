package dev.danya.museum.feature.artworks.data.di

import dev.danya.museum.feature.artworks.data.local.ArtworkLocalDataSource
import dev.danya.museum.feature.artworks.data.local.ExhibitLocalDataSource
import dev.danya.museum.feature.artworks.data.remote.ArtworkApiService
import dev.danya.museum.feature.artworks.data.repository.ArtworkRepositoryImpl
import dev.danya.museum.feature.artworks.data.repository.ExhibitRepositoryImpl
import dev.danya.museum.feature.artworks.domain.repository.ArtworkRepository
import dev.danya.museum.feature.artworks.domain.repository.ExhibitRepository
import org.koin.dsl.module

val artworksDataModule = module {
    single { ArtworkApiService(get()) }
    single { ArtworkLocalDataSource(get(), get()) }
    single { ExhibitLocalDataSource(get(), get()) }
    single<ArtworkRepository> { ArtworkRepositoryImpl(get(), get()) }
    single<ExhibitRepository> { ExhibitRepositoryImpl(get()) }
}
