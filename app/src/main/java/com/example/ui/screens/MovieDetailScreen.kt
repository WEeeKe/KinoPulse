package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.MovieEntity
import com.example.data.recommendations.RecommendedMovie
import com.example.ui.components.MinimalIconButton
import com.example.ui.components.MinimalPanel
import com.example.ui.components.MinimalPill
import com.example.ui.components.MinimalPrimaryButton
import com.example.ui.components.MinimalSecondaryButton
import com.example.ui.components.MovieArtworkImage
import com.example.ui.components.RecommendationCard
import com.example.ui.icons.LucideIcons
import com.example.ui.theme.LocalKinoPalette

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MovieDetailScreen(
    movie: MovieEntity,
    relatedMovies: List<RecommendedMovie>,
    downloadProgress: Float?,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSelectCollection: (String) -> Unit,
    onDownload: () -> Unit,
    onRemoveDownload: () -> Unit,
    onOpenMovieChat: () -> Unit,
    onGenreClick: (String) -> Unit,
    onOpenRelatedMovie: (String) -> Unit,
    onPlayRelatedMovie: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalKinoPalette.current
    val collections = listOf("Любимые", "Смотреть позже", "На выходные")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background),
        contentPadding = PaddingValues(bottom = 42.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                MovieArtworkImage(
                    posterKey = movie.posterKey,
                    posterUrl = movie.posterUrl,
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.45f),
                                    Color.Transparent,
                                    palette.background.copy(alpha = 0.85f),
                                    palette.background
                                )
                            )
                        )
                )

                MinimalIconButton(
                    icon = LucideIcons.ChevronLeft,
                    contentDescription = "Назад",
                    onClick = onBack,
                    containerColor = Color.Black.copy(alpha = 0.48f),
                    contentColor = Color.White,
                    borderColor = Color.White.copy(alpha = 0.16f),
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(14.dp)
                        .testTag("detail_back_button")
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = LucideIcons.Star,
                                contentDescription = null,
                                tint = palette.ratingGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = movie.rating.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "·  ${movie.releaseYear}  ·  ${movie.durationMinutes} мин" +
                                if (movie.country.isNotBlank()) "  ·  ${movie.country}" else "",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.textSecondary
                        )
                    }

                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.displaySmall,
                        color = palette.textPrimary
                    )
                    if (movie.originalTitle.isNotBlank() && movie.originalTitle != movie.title) {
                        Text(
                            text = movie.originalTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textTertiary
                        )
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                val playLabel = when {
                    movie.watchProgressSeconds > 0L && movie.isDownloaded -> {
                        val m = movie.watchProgressSeconds / 60
                        val s = movie.watchProgressSeconds % 60
                        "Продолжить офлайн (%02d:%02d)".format(m, s)
                    }
                    movie.isDownloaded -> "Смотреть офлайн"
                    movie.watchProgressSeconds > 0L -> {
                        val m = movie.watchProgressSeconds / 60
                        val s = movie.watchProgressSeconds % 60
                        "Продолжить с %02d:%02d".format(m, s)
                    }
                    else -> "Смотреть"
                }

                MinimalPrimaryButton(
                    text = playLabel,
                    icon = LucideIcons.Play,
                    onClick = onPlay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("detail_play_button")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MinimalSecondaryButton(
                        text = if (movie.isFavorite) "В избранном" else "В избранное",
                        icon = if (movie.isFavorite) LucideIcons.BookmarkFilled else LucideIcons.Bookmark,
                        activeTint = if (movie.isFavorite) palette.accent else null,
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("detail_fav_button")
                    )

                    MinimalSecondaryButton(
                        text = if (movie.isDownloaded) "Офлайн (${String.format("%.0f", movie.downloadSizeMb)} МБ)" else "Скачать",
                        icon = if (movie.isDownloaded) LucideIcons.CheckCircle else LucideIcons.Download,
                        activeTint = if (movie.isDownloaded) palette.onlineGreen else null,
                        enabled = downloadProgress == null,
                        onClick = { if (movie.isDownloaded) onRemoveDownload() else onDownload() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("detail_download_button")
                    )
                }

                if (downloadProgress != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            color = palette.accent,
                            trackColor = palette.surfaceElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Text(
                            text = "Сохранение на устройство: ${(downloadProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textSecondary
                        )
                    }
                }

                MinimalSecondaryButton(
                    text = "Обсудить в киноклубе",
                    icon = LucideIcons.MessageCircle,
                    onClick = onOpenMovieChat,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("detail_chat_button")
                )

                // Interactive Genre Pills (clicking filters catalog by genre!)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    movie.genres.forEach { genre ->
                        MinimalPill(
                            text = genre,
                            selected = false,
                            onClick = { onGenreClick(genre) }
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "О фильме",
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary
                    )
                    Text(
                        text = movie.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = palette.textSecondary
                    )
                }

                MinimalPanel(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetaInfoRow("Режиссёр", movie.director)
                        HorizontalDivider(color = palette.borderSubtle)
                        MetaInfoRow("В ролях", movie.cast.joinToString(", "))
                        if (movie.archiveId.isNotBlank()) {
                            HorizontalDivider(color = palette.borderSubtle)
                            MetaInfoRow("Источник", "Internet Archive · ${movie.archiveId}")
                        }
                    }
                }

                // Collection Selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Коллекция",
                        style = MaterialTheme.typography.titleSmall,
                        color = palette.textSecondary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        collections.forEach { col ->
                            MinimalPill(
                                text = col,
                                selected = movie.isFavorite && movie.userCollection == col,
                                onClick = { onSelectCollection(col) }
                            )
                        }
                    }
                }

                if (relatedMovies.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Похожие картины",
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textPrimary
                    )
                    relatedMovies.forEach { rec ->
                        RecommendationCard(
                            item = rec,
                            onClick = { onOpenRelatedMovie(rec.movie.id) },
                            onPlayClick = { onPlayRelatedMovie(rec.movie.id) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaInfoRow(label: String, value: String) {
    val palette = LocalKinoPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = palette.textTertiary,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = palette.textPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}
