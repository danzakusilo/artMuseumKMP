package dev.danya.museum.feature.artworks.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ArtworkDetailDto(
    val objectID: Int,
    val title: String,
    val primaryImage: String? = null,
    val primaryImageSmall: String? = null,
    val artistDisplayName: String? = null,
    val objectDate: String? = null,
    val culture: String? = null,
    val period: String? = null,
    val dynasty: String? = null,
    val medium: String? = null,
    val dimensions: String? = null,
    val department: String,
    val classification: String? = null,
    val repository: String? = null,
)
