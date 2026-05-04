package dev.danya.museum.feature.homescreen.ui

data class HomeScreenState(
    val query: String = "",
    val isLoading: Boolean = false,
    val result: String = "",
)
