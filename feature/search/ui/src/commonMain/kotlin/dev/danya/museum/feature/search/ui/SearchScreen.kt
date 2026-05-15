package dev.danya.museum.feature.search.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import demo.core.ui.generated.resources.Res
import demo.core.ui.generated.resources.error_generic_subtitle
import demo.core.ui.generated.resources.error_generic_title
import demo.core.ui.generated.resources.error_no_internet_subtitle
import demo.core.ui.generated.resources.error_no_internet_title
import demo.core.ui.generated.resources.error_retry
import dev.danya.museum.core.common.result.AppError
import dev.danya.museum.core.ui.component.ArtworkCard
import org.jetbrains.compose.resources.stringResource
import dev.danya.museum.feature.artworks.domain.entity.ArtworkSummary
import dev.danya.museum.feature.artworks.domain.entity.Department
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchScreen(
    onNavigateToDetail: (Int) -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        SearchBar(
            query = state.query,
            onQueryChange = viewModel::onQueryChange,
            onSearch = viewModel::onSearch,
            artistOrCulture = state.artistOrCulture,
        )
        Spacer(Modifier.height(8.dp))
        FilterChipsRow(
            selectedDepartment = state.selectedDepartment,
            onDepartmentSelected = viewModel::onDepartmentSelected,
            hasImages = state.hasImages,
            onHasImagesToggled = viewModel::onHasImagesToggled,
            artistOrCulture = state.artistOrCulture,
            onArtistOrCultureToggled = viewModel::onArtistOrCultureToggled,
        )
        Spacer(Modifier.height(8.dp))
        ResultsContent(
            resultState = state.resultState,
            onNavigateToDetail = onNavigateToDetail,
            onRetry = viewModel::onSearch,
            onLoadMore = viewModel::onLoadMore,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    artistOrCulture: Boolean,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(if (artistOrCulture) "Artist or culture name…" else "Search artworks…")
        },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
    )
}

@Composable
private fun FilterChipsRow(
    selectedDepartment: Department?,
    onDepartmentSelected: (Department?) -> Unit,
    hasImages: Boolean,
    onHasImagesToggled: () -> Unit,
    artistOrCulture: Boolean,
    onArtistOrCultureToggled: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
    ) {
        DepartmentChip(
            selected = selectedDepartment,
            onSelected = onDepartmentSelected,
        )
        FilterChip(
            selected = hasImages,
            onClick = onHasImagesToggled,
            label = { Text("Has image") },
        )
        FilterChip(
            selected = artistOrCulture,
            onClick = onArtistOrCultureToggled,
            label = { Text("Artist/Culture") },
        )
    }
}

@Composable
private fun DepartmentChip(
    selected: Department?,
    onSelected: (Department?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        FilterChip(
            selected = selected != null,
            onClick = {
                if (selected != null) onSelected(null) else expanded = true
            },
            leadingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            },
            label = { Text(selected?.name?.replace('_', ' ')?.lowercase()
                ?.replaceFirstChar { it.uppercase() } ?: "Department") },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            Department.entries.forEach { dept ->
                DropdownMenuItem(
                    text = {
                        Text(
                            dept.name.replace('_', ' ').lowercase()
                                .replaceFirstChar { it.uppercase() },
                        )
                    },
                    onClick = {
                        onSelected(dept)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ResultsContent(
    resultState: ResultState,
    onNavigateToDetail: (Int) -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (resultState) {
        is ResultState.Idle -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Search for artworks in the Met collection",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        is ResultState.Loading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ResultState.Error -> {
            val isNoInternet = resultState.error is AppError.NoInternetError
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    Button(onClick = onRetry) {
                        Text(stringResource(Res.string.error_retry))
                    }
                }
            }
        }
        is ResultState.Content -> {
            if (resultState.results.isEmpty()) {
                Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No results found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                ResultsList(
                    results = resultState.results,
                    hasMore = resultState.hasMore,
                    isLoadingMore = resultState.isLoadingMore,
                    onNavigateToDetail = onNavigateToDetail,
                    onLoadMore = onLoadMore,
                    modifier = modifier,
                )
            }
        }
    }
}

@Composable
private fun ResultsList(
    results: List<ArtworkSummary>,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onNavigateToDetail: (Int) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(results, key = { it.id }) { artwork ->
            ArtworkCard(
                title = artwork.title,
                imageUrl = artwork.primaryImageUrl,
                artistName = artwork.artistName,
                objectDate = artwork.objectDate,
                modifier = Modifier.fillMaxWidth().clickable { onNavigateToDetail(artwork.id) },
            )
        }
        if (hasMore) {
            item {
                Box(
                    Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isLoadingMore) {
                        CircularProgressIndicator(Modifier.size(24.dp))
                    } else {
                        Button(onClick = onLoadMore) { Text("Load more") }
                    }
                }
            }
        }
    }
}
