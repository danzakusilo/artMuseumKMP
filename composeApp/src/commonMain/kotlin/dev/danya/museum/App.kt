package dev.danya.museum

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.danya.museum.core.common.di.commonModule
import dev.danya.museum.core.database.di.databaseModule
import dev.danya.museum.core.network.di.networkModule
import dev.danya.museum.core.ui.theme.MuseumTheme
import dev.danya.museum.feature.artworks.data.di.artworksDataModule
import dev.danya.museum.feature.homescreen.ui.di.homescreenPresentationModule
import dev.danya.museum.navigation.RootNavHost
import org.koin.compose.KoinApplication
import org.koin.core.logger.Level
import org.koin.dsl.KoinConfiguration

@Composable
@Preview
fun App(platformSetup: org.koin.core.KoinApplication.() -> Unit = {}) {
    KoinApplication( configuration = KoinConfiguration{
        platformSetup()
        printLogger(Level.DEBUG)
        modules(
            commonModule,
            networkModule,
            databaseModule,
            artworksDataModule,
            homescreenPresentationModule,
        )
    }) {
        MuseumTheme {
            RootNavHost()
        }
    }
}