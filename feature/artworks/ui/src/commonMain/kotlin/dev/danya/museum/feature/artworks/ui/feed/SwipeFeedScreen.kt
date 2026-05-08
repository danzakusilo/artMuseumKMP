package dev.danya.museum.feature.artworks.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.danya.museum.feature.artworks.domain.entity.Artwork
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SwipeFeedScreen(viewModel: SwipeFeedViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()

    when (val s = state) {
        is SwipeFeedState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is SwipeFeedState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Something went wrong", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = viewModel::retry) { Text("Retry") }
                }
            }
        }
        is SwipeFeedState.Content -> {
            FeedPager(
                artworks = s.artworks,
                favorites = s.favorites,
                onPageSettled = viewModel::onPageSettled,
                onToggleFavorite = viewModel::onToggleFavorite,
            )
        }
    }
}

@Composable
private fun FeedPager(
    artworks: List<Artwork>,
    favorites: Set<Int>,
    onPageSettled: (Int) -> Unit,
    onToggleFavorite: (Int) -> Unit,
) {
    val pagerState = rememberPagerState { artworks.size }

    LaunchedEffect(pagerState.settledPage) {
        onPageSettled(pagerState.settledPage)
    }

    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
    ) { page ->
        val artwork = artworks[page]
        FeedPage(
            artwork = artwork,
            isFavorite = artwork.id in favorites,
            onToggleFavorite = { onToggleFavorite(artwork.id) },
        )
    }
}

@Composable
private fun FeedPage(
    artwork: Artwork,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = artwork.primaryImageUrl,
            contentDescription = artwork.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, end = 72.dp, bottom = 32.dp),
        ) {
            Text(
                text = artwork.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (artwork.artistName != null) {
                Text(
                    text = artwork.artistName ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (artwork.objectDate != null) {
                Text(
                    text = artwork.objectDate ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                )
            }
        }

        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 32.dp)
                .size(48.dp),
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                tint = if (isFavorite) MaterialTheme.colorScheme.error else Color.White,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}
