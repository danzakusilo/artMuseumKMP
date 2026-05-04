package dev.danya.museum.feature.artworks.data.mapper

import dev.danya.museum.core.database.Artwork as ArtworkEntity
import dev.danya.museum.feature.artworks.data.remote.dto.ArtworkDetailDto
import dev.danya.museum.feature.artworks.domain.entity.Artwork
import dev.danya.museum.feature.artworks.domain.entity.ArtworkSummary

fun ArtworkDetailDto.toDomain(): Artwork = Artwork(
    id = objectID,
    title = title,
    primaryImageUrl = primaryImageSmall?.takeIf { it.isNotBlank() } ?: primaryImage?.takeIf { it.isNotBlank() },
    artistName = artistDisplayName?.takeIf { it.isNotBlank() },
    objectDate = objectDate?.takeIf { it.isNotBlank() },
    culture = culture?.takeIf { it.isNotBlank() },
    period = period?.takeIf { it.isNotBlank() },
    dynasty = dynasty?.takeIf { it.isNotBlank() },
    medium = medium?.takeIf { it.isNotBlank() },
    dimensions = dimensions?.takeIf { it.isNotBlank() },
    department = department,
    classification = classification?.takeIf { it.isNotBlank() },
    repository = repository?.takeIf { it.isNotBlank() },
)

fun ArtworkDetailDto.toSummary(): ArtworkSummary = ArtworkSummary(
    id = objectID,
    title = title,
    primaryImageUrl = primaryImageSmall?.takeIf { it.isNotBlank() } ?: primaryImage?.takeIf { it.isNotBlank() },
    artistName = artistDisplayName?.takeIf { it.isNotBlank() },
    objectDate = objectDate?.takeIf { it.isNotBlank() },
)

fun ArtworkEntity.toSummary(): ArtworkSummary = ArtworkSummary(
    id = id.toInt(),
    title = title,
    primaryImageUrl = primaryImage,
    artistName = artistDisplayName,
    objectDate = objectDate,
)

fun ArtworkEntity.toDomain(): Artwork = Artwork(
    id = id.toInt(),
    title = title,
    primaryImageUrl = primaryImage,
    artistName = artistDisplayName,
    objectDate = objectDate,
    culture = culture,
    period = period,
    dynasty = dynasty,
    medium = medium,
    dimensions = dimensions,
    department = department,
    classification = classification,
    repository = repository,
)
