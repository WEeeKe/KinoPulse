package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.MovieEntity
import com.example.ui.MainUiState
import com.example.ui.components.MinimalIconButton
import com.example.ui.components.MinimalPanel
import com.example.ui.components.MinimalPill
import com.example.ui.components.MinimalSegmentedBar
import com.example.ui.components.MovieArtworkImage
import com.example.ui.components.bounceClick
import com.example.ui.icons.LucideIcons
import com.example.ui.theme.LocalKinoPalette

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
    val palette = LocalKinoPalette.current
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    var selectedCollection by remember { mutableStateOf("Все") }
    val collections = listOf("Все", "Любимые", "Смотреть позже", "На выходные")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        MinimalSegmentedBar(
            options = listOf(
                "Избранное (${uiState.favorites.size})",
                "Офлайн (${uiState.downloadedMovies.size})"
            ),
            selectedIndex = selectedTab,
            onSelect = { selectedTab = it },
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .testTag("tab_favorites")
        )

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "fav_tab_anim"
        ) { tab ->
            if (tab == 0) {
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(collections) { collection ->
                            MinimalPill(
                                text = collection,
                                selected = selectedCollection == collection,
                                onClick = { selectedCollection = collection }
                            )
                        }
                    }

                    val displayed = if (selectedCollection == "Все") {
                        uiState.favorites
                    } else {
                        uiState.favorites.filter { it.userCollection == selectedCollection }
                    }

                    if (displayed.isEmpty()) {
                        EmptyMinimalState(
                            icon = LucideIcons.Bookmark,
                            title = "Коллекция пуста",
                            subtitle = "Нажмите на значок закладки на любом фильме, чтобы сохранить его здесь."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = 18.dp,
                                end = 18.dp,
                                top = 10.dp,
                                bottom = 112.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(displayed, key = { it.id }) { movie ->
                                SavedMinimalRow(
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
            } else {
                if (uiState.downloadedMovies.isEmpty() && uiState.downloadProgress.isEmpty()) {
                    EmptyMinimalState(
                        icon = LucideIcons.Download,
                        title = "Нет офлайн-загрузок",
                        subtitle = "Скачивайте фильмы на карточке картины — они будут доступны без интернета."
                    )
                } else {
                    val activeAndDownloaded = (uiState.downloadedMovies + uiState.allMovies.filter {
                        uiState.downloadProgress.containsKey(it.id)
                    }).distinctBy { it.id }

                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 18.dp,
                            end = 18.dp,
                            top = 10.dp,
                            bottom = 112.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(activeAndDownloaded, key = { "offline_${it.id}" }) { movie ->
                            SavedMinimalRow(
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
}

@Composable
private fun SavedMinimalRow(
    movie: MovieEntity,
    downloadProgress: Float?,
    onClick: () -> Unit,
    onPlay: () -> Unit,
    onDownloadOrRemove: () -> Unit,
    onRemoveFavorite: () -> Unit
) {
    val palette = LocalKinoPalette.current
    MinimalPanel(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(scaleDown = 0.98f, onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MovieArtworkImage(
                    posterKey = movie.posterKey,
                    posterUrl = movie.posterUrl,
                    contentDescription = movie.title,
                    modifier = Modifier
                        .width(66.dp)
                        .height(94.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${movie.releaseYear} · ${movie.durationMinutes} мин · ★ ${movie.rating}",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (movie.isFavorite) {
                            Text(
                                text = movie.userCollection,
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textSecondary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(palette.surfaceElevated)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                        if (movie.isDownloaded) {
                            Text(
                                text = "${String.format("%.1f", movie.downloadSizeMb)} МБ",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.onlineGreen,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(palette.surfaceElevated)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    MinimalIconButton(
                        icon = LucideIcons.Play,
                        contentDescription = "Смотреть",
                        onClick = onPlay,
                        containerColor = palette.ctaBackground,
                        contentColor = palette.ctaContent,
                        borderColor = palette.borderSubtle,
                        size = 38.dp,
                        iconSize = 15.dp
                    )
                    MinimalIconButton(
                        icon = if (movie.isDownloaded) LucideIcons.CheckCircle else LucideIcons.Download,
                        contentDescription = if (movie.isDownloaded) "Удалить офлайн" else "Скачать",
                        onClick = { if (downloadProgress == null) onDownloadOrRemove() },
                        contentColor = if (movie.isDownloaded) palette.onlineGreen else palette.textSecondary,
                        size = 38.dp,
                        iconSize = 17.dp
                    )
                    if (movie.isFavorite) {
                        MinimalIconButton(
                            icon = LucideIcons.Trash,
                            contentDescription = "Убрать",
                            onClick = onRemoveFavorite,
                            contentColor = palette.accent,
                            size = 38.dp,
                            iconSize = 17.dp
                        )
                    }
                }
            }

            if (downloadProgress != null) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { downloadProgress },
                    color = palette.accent,
                    trackColor = palette.surfaceElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Загрузка: ${(downloadProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary
                )
            }
        }
    }
}

@Composable
private fun EmptyMinimalState(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    val palette = LocalKinoPalette.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = palette.textTertiary,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
    }
}
