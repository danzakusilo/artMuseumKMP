package dev.danya.museum.feature.artworks.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SearchResultDto(
    val total: Int,
    val objectIDs: List<Int>? = null,
)
