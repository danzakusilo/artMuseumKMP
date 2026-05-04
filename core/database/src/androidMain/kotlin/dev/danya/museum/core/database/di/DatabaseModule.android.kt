package dev.danya.museum.core.database.di

import dev.danya.museum.core.database.DatabaseDriverFactory
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformDatabaseModule(): Module = module {
    single { DatabaseDriverFactory(androidApplication()) }
}
