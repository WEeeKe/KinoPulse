package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.MovieEntity
import com.example.ui.MainUiState
import com.example.ui.components.MovieArtworkImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    uiState: MainUiState,
    initialTab: Int = 0,
    onMovieClick: (String) -> Unit,
    onPlayClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onDownloadMovie: (MovieEntity) -> Unit,
    onRemoveDownload: (MovieEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    var selectedCollection by remember { mutableStateOf("Все") }
    val collections = listOf("Все", "Любимые", "Смотреть позже", "На выходные")

    Column(modifier = modifier.fillMaxSize()) {
        SecondaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Избранное (${uiState.favorites.size})") },
                modifier = Modifier.testTag("tab_favorites")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Офлайн (${uiState.downloadedMovies.size})") },
                modifier = Modifier.testTag("tab_offline")
            )
        }

        if (selectedTab == 0) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(collections) { collection ->
                    FilterChip(
                        selected = selectedCollection == collection,
                        onClick = { selectedCollection = collection },
                        label = { Text(collection) }
                    )
                }
            }

            val displayed = if (selectedCollection == "Все") {
                uiState.favorites
            } else {
                uiState.favorites.filter { it.userCollection == selectedCollection }
            }

            if (displayed.isEmpty()) {
                EmptyCollectionState(
                    icon = Icons.Filled.Bookmark,
                    title = "Список пока пуст",
                    subtitle = "Нажмите на значок закладки на карточке фильма, чтобы добавить его в личную коллекцию."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayed, key = { it.id }) { movie ->
                        SavedMovieRowCard(
                            movie = movie,
                            downloadProgress = uiState.downloadProgress[movie.id],
                            onClick = { onMovieClick(movie.id) },
                            onPlay = { onPlayClick(movie.id) },
                            onDownloadOrRemove = {
                                if (movie.isDownloaded) onRemoveDownload(movie) else onDownloadMovie(movie)
                            },
                            onRemoveFavorite = { onToggleFavorite(movie.id) }
                        )
                    }
                }
            }
        } else {
            if (uiState.downloadedMovies.isEmpty() && uiState.downloadProgress.isEmpty()) {
                EmptyCollectionState(
                    icon = Icons.Filled.CloudDownload,
                    title = "Нет загруженных фильмов",
                    subtitle = "Скачайте фильмы для просмотра без интернета. Их можно будет запустить в любое время в офлайн-режиме."
                )
            } else {
                val activeAndDownloaded = (uiState.downloadedMovies + uiState.allMovies.filter {
                    uiState.downloadProgress.containsKey(it.id)
                }).distinctBy { it.id }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(activeAndDownloaded, key = { "offline_${it.id}" }) { movie ->
                        SavedMovieRowCard(
                            movie = movie,
                            downloadProgress = uiState.downloadProgress[movie.id],
                            onClick = { onMovieClick(movie.id) },
                            onPlay = { onPlayClick(movie.id) },
                            onDownloadOrRemove = {
                                if (movie.isDownloaded) onRemoveDownload(movie) else onDownloadMovie(movie)
                            },
                            onRemoveFavorite = { onToggleFavorite(movie.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedMovieRowCard(
    movie: MovieEntity,
    downloadProgress: Float?,
    onClick: () -> Unit,
    onPlay: () -> Unit,
    onDownloadOrRemove: () -> Unit,
    onRemoveFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MovieArtworkImage(
                    posterKey = movie.posterKey,
                    posterUrl = movie.posterUrl,
                    contentDescription = movie.title,
                    modifier = Modifier
                        .width(74.dp)
                        .height(104.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${movie.releaseYear} · ${movie.genres.take(2).joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (movie.isFavorite) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = movie.userCollection,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        if (movie.isDownloaded) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "${String.format("%.1f", movie.downloadSizeMb)} МБ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onPlay,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Смотреть",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onDownloadOrRemove,
                        enabled = downloadProgress == null,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = if (movie.isDownloaded) Icons.Filled.DownloadDone else Icons.Filled.CloudDownload,
                            contentDescription = if (movie.isDownloaded) "Удалить офлайн" else "Скачать",
                            tint = if (movie.isDownloaded) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (movie.isFavorite) {
                        IconButton(
                            onClick = onRemoveFavorite,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Убрать из избранного",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }

            if (downloadProgress != null) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { downloadProgress },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Загрузка для офлайн-просмотра: ${(downloadProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun EmptyCollectionState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
