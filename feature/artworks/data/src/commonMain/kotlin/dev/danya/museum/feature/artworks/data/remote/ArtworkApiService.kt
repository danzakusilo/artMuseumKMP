package dev.danya.museum.feature.artworks.data.remote

import dev.danya.museum.feature.artworks.data.remote.dto.ArtworkDetailDto
import dev.danya.museum.feature.artworks.data.remote.dto.SearchResultDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

private const val BASE_URL = "https://collectionapi.metmuseum.org/public/collection/v1"
private const val SEARCH_RESULT_LIMIT = 20
private const val RECENT_ARTWORKS_LIMIT = 20
private const val FETCH_CONCURRENCY = 5

class ArtworkApiService(private val client: HttpClient) {

    suspend fun search(query: String): SearchResultDto =
        client.get("$BASE_URL/search") {
            parameter("q", query)
            parameter("hasImages", true)
        }.body()

    suspend fun getObjectSingle(id: Int): ArtworkDetailDto =
        client.get("$BASE_URL/objects/$id").body()

    suspend fun fetchArtworkDetails(ids: List<Int>): List<ArtworkDetailDto> = coroutineScope {
        ids.chunked(FETCH_CONCURRENCY)
            .flatMap { chunk ->
                chunk.map { id -> async { runCatching { getObjectSingle(id) }.getOrNull() } }
                    .awaitAll()
            }
            .filterNotNull()
    }

    suspend fun fetchDepartmentObjectIds(departmentId: Int): List<Int> =
        client.get("$BASE_URL/objects") {
            parameter("departmentIds", departmentId)
        }.body<SearchResultDto>().objectIDs.orEmpty()

    suspend fun searchAndFetch(query: String): List<ArtworkDetailDto> {
        val ids = search(query).objectIDs?.take(SEARCH_RESULT_LIMIT) ?: return emptyList()
        return fetchArtworkDetails(ids)
    }

    suspend fun getRecentObjects(metadataDate: String): SearchResultDto =
        client.get("$BASE_URL/objects") {
            parameter("metadataDate", metadataDate)
        }.body()

    suspend fun fetchRecentArtworks(metadataDate: String): List<ArtworkDetailDto> {
        val ids = getRecentObjects(metadataDate).objectIDs?.take(RECENT_ARTWORKS_LIMIT) ?: return emptyList()
        return fetchArtworkDetails(ids)
    }
}
