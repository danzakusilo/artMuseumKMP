package dev.danya.museum.feature.artworks.data.remote

import dev.danya.museum.feature.artworks.data.remote.dto.ArtworkDetailDto
import dev.danya.museum.feature.artworks.data.remote.dto.SearchResultDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

private const val BASE_URL = "https://collectionapi.metmuseum.org/public/collection/v1"
private const val SEARCH_RESULT_LIMIT = 20

class ArtworkApiService(private val client: HttpClient) {

    suspend fun search(query: String): SearchResultDto =
        client.get("$BASE_URL/search") {
            parameter("q", query)
            parameter("hasImages", true)
        }.body()

    suspend fun getObject(id: Int): ArtworkDetailDto =
        client.get("$BASE_URL/objects/$id").body()

    suspend fun searchAndFetch(query: String): List<ArtworkDetailDto> {
        val ids = search(query).objectIDs?.take(SEARCH_RESULT_LIMIT) ?: return emptyList()
        return ids.map { getObject(it) }
    }
}
