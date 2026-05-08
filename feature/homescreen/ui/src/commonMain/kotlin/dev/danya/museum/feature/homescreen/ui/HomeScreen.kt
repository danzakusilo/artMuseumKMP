package dev.danya.museum.feature.homescreen.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.danya.museum.core.ui.component.ArtworkCard
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(viewModel: HomeScreenViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Recently Updated",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        when {
            state.isLoadingRecents -> CircularProgressIndicator()
            state.recentsError -> Text(
                text = "Couldn't load recent artworks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )

            state.recentArtworks.isNotEmpty() -> LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.recentArtworks) { artwork ->
                    ArtworkCard(
                        title = artwork.title,
                        imageUrl = artwork.primaryImageUrl,
                        artistName = artwork.artistName,
                        objectDate = artwork.objectDate,
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            label = { Text("Search query") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = viewModel::search,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.isLoading) "Loading…" else "Fetch from API")
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.result,
            onValueChange = {},
            readOnly = true,
            label = { Text("Response") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}