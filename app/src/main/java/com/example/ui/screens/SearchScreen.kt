package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.MainUiState
import com.example.ui.SortOption
import com.example.ui.YearFilterPreset
import com.example.ui.components.MoviePosterCard
import kotlin.math.roundToInt

@Composable
fun SearchScreen(
    uiState: MainUiState,
    onQueryChange: (String) -> Unit,
    onToggleGenre: (String) -> Unit,
    onSelectYearPreset: (YearFilterPreset) -> Unit,
    onCustomYearRange: (Int, Int) -> Unit,
    onSelectSort: (SortOption) -> Unit,
    onToggleOfflineOnly: () -> Unit,
    onResetFilters: () -> Unit,
    onMovieClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filter = uiState.searchFilter

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 156.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = filter.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input"),
                    placeholder = { Text("Название, режиссёр, жанр или год...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (filter.query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(imageVector = Icons.Filled.Clear, contentDescription = "Очистить")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Text(
                    text = "Жанры",
                    style = MaterialTheme.typography.titleSmall
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.availableGenres) { genre ->
                        FilterChip(
                            selected = genre in filter.selectedGenres,
                            onClick = { onToggleGenre(genre) },
                            label = { Text(genre) },
                            modifier = Modifier.testTag("search_genre_$genre")
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Год выпуска: ${filter.yearStart}–${filter.yearEnd}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (filter.selectedGenres.isNotEmpty() ||
                        filter.yearPreset != YearFilterPreset.ALL ||
                        filter.query.isNotEmpty() ||
                        filter.offlineOnly
                    ) {
                        TextButton(onClick = onResetFilters) {
                            Text("Сбросить")
                        }
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(YearFilterPreset.entries.filter { it != YearFilterPreset.CUSTOM }) { preset ->
                        FilterChip(
                            selected = filter.yearPreset == preset,
                            onClick = { onSelectYearPreset(preset) },
                            label = { Text(preset.label) },
                            modifier = Modifier.testTag("search_year_${preset.name}")
                        )
                    }
                }

                RangeSlider(
                    value = filter.yearStart.toFloat()..filter.yearEnd.toFloat(),
                    onValueChange = { range ->
                        onCustomYearRange(range.start.roundToInt(), range.endInclusive.roundToInt())
                    },
                    valueRange = 1994f..2026f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("year_range_slider")
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SortOption.entries) { option ->
                        FilterChip(
                            selected = filter.sortBy == option,
                            onClick = { onSelectSort(option) },
                            label = { Text(option.label) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filter.offlineOnly,
                            onClick = onToggleOfflineOnly,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.CloudDone,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text("Только офлайн") }
                        )
                    }
                }

                Text(
                    text = "Найдено фильмов: ${uiState.filteredMovies.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (uiState.filteredMovies.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FilterListOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "Ничего не найдено",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Попробуйте изменить выбранные жанры или расширить диапазон годов выпуска",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(onClick = onResetFilters) {
                            Text("Сбросить фильтры")
                        }
                    }
                }
            }
        } else {
            items(uiState.filteredMovies, key = { it.id }) { movie ->
                MoviePosterCard(
                    movie = movie,
                    onClick = { onMovieClick(movie.id) },
                    onToggleFavorite = { onToggleFavorite(movie.id) }
                )
            }
        }
    }
}
