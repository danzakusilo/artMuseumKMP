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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import demo.core.ui.generated.resources.feed_shuffle
import dev.danya.museum.feature.artworks.domain.entity.Department

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
            var showDepartmentPicker by remember { mutableStateOf(false) }

            Box {
                FeedPager(
                    artworks = s.artworks,
                    favorites = s.favorites,
                    onPageSettled = viewModel::onPageSettled,
                    onToggleFavorite = viewModel::onToggleFavorite,
                    onNavigateToDetail = onNavigateToDetail,
                )

                DepartmentHeader(
                    department = s.currentDepartment,
                    onTap = { showDepartmentPicker = true },
                    onShuffle = { viewModel.onChangeDepartment(null) },
                    modifier = Modifier.align(Alignment.TopStart),
                )
            }

            if (showDepartmentPicker) {
                DepartmentPickerSheet(
                    currentDepartment = s.currentDepartment,
                    onSelectDepartment = { dept ->
                        viewModel.onChangeDepartment(dept)
                        showDepartmentPicker = false
                    },
                    onDismiss = { showDepartmentPicker = false },
                )
            }
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

@Composable
private fun DepartmentHeader(
    department: Department,
    onTap: () -> Unit,
    onShuffle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onTap)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(department.stringResource()),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onShuffle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = stringResource(Res.string.feed_shuffle),
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
