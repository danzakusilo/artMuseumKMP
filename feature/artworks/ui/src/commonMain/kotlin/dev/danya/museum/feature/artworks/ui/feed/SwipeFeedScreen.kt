package dev.danya.museum.feature.artworks.ui.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import demo.core.ui.generated.resources.Res
import demo.core.ui.generated.resources.error_generic_subtitle
import demo.core.ui.generated.resources.error_generic_title
import demo.core.ui.generated.resources.error_no_internet_subtitle
import demo.core.ui.generated.resources.error_no_internet_title
import demo.core.ui.generated.resources.error_retry
import dev.danya.museum.core.common.result.AppError
import org.jetbrains.compose.resources.stringResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.danya.museum.feature.artworks.domain.entity.Artwork
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SwipeFeedScreen(
    onNavigateToDetail: (Int) -> Unit,
    viewModel: SwipeFeedViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    when (val s = state) {
        is SwipeFeedState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is SwipeFeedState.Error -> {
            val isNoInternet = s.error is AppError.NoInternetError
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(
                            if (isNoInternet) Res.string.error_no_internet_title
                            else Res.string.error_generic_title,
                        ),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            if (isNoInternet) Res.string.error_no_internet_subtitle
                            else Res.string.error_generic_subtitle,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = viewModel::retry) {
                        Text(stringResource(Res.string.error_retry))
                    }
                }
            }
        }
        is SwipeFeedState.Content -> {
            FeedPager(
                artworks = s.artworks,
                favorites = s.favorites,
                onPageSettled = viewModel::onPageSettled,
                onToggleFavorite = viewModel::onToggleFavorite,
                onNavigateToDetail = onNavigateToDetail,
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
    onNavigateToDetail: (Int) -> Unit,
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
            onClick = { onNavigateToDetail(artwork.id) },
        )
    }
}
