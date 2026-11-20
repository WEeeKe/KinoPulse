package com.example.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.MovieEntity
import com.example.ui.MainUiState
import com.example.ui.components.MinimalIconButton
import com.example.ui.components.MinimalPanel
import com.example.ui.components.MinimalPill
import com.example.ui.components.MinimalPrimaryButton
import com.example.ui.components.MinimalSecondaryButton
import com.example.ui.components.MinimalSectionHeader
import com.example.ui.components.MovieArtworkImage
import com.example.ui.components.MoviePosterCard
import com.example.ui.components.RecommendationCard
import com.example.ui.components.bounceClick
import com.example.ui.icons.LucideIcons
import com.example.ui.theme.LocalKinoPalette
import kotlin.math.absoluteValue

@Composable
fun HomeScreen(
    uiState: MainUiState,
    onMovieClick: (String) -> Unit,
    onPlayClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onGenreClick: (String) -> Unit,
    onOpenOfflineTab: () -> Unit,
    onOpenSearch: () -> Unit,
    onSyncMoreOnline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalKinoPalette.current
    val prefs = uiState.preferences

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background),
        contentPadding = PaddingValues(top = 6.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(26.dp)
    ) {
        if (uiState.effectiveOfflineMode) {
            item {
                MinimalOfflineBanner(
                    downloadedCount = uiState.downloadedMovies.size,
                    isManualOffline = prefs.offlineModeEnabled,
                    onOpenOfflineTab = onOpenOfflineTab,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }
        }

        // Animated Hero Pager Showcase
        if (uiState.featuredMovies.isNotEmpty()) {
            item {
                val pagerState = rememberPagerState(pageCount = { uiState.featuredMovies.size })
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 18.dp),
                        pageSpacing = 12.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        val movie = uiState.featuredMovies[page]
                        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                        val scale = 1f - (pageOffset.coerceIn(0f, 1f) * 0.04f)
                        val alpha = 1f - (pageOffset.coerceIn(0f, 1f) * 0.18f)

                        HeroCinemaCard(
                            movie = movie,
                            onOpenDetail = { onMovieClick(movie.id) },
                            onPlay = { onPlayClick(movie.id) },
                            onToggleFavorite = { onToggleFavorite(movie.id) },
                            modifier = Modifier.graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                            }
                        )
                    }

                    // Minimal animated pill page indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(uiState.featuredMovies.size) { idx ->
                            val isSelected = pagerState.currentPage == idx
                            val width by animateDpAsState(
                                targetValue = if (isSelected) 20.dp else 6.dp,
                                label = "dot_w_$idx"
                            )
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .height(5.dp)
                                    .width(width)
                                    .clip(CircleShape)
                                    .background(if (isSelected) palette.textPrimary else palette.border)
                            )
                        }
                    }
                }
            }
        }

        // Minimal Genre Pills
        if (uiState.availableGenres.isNotEmpty()) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.availableGenres) { genre ->
                        MinimalPill(
                            text = genre,
                            selected = false,
                            onClick = { onGenreClick(genre) },
                            modifier = Modifier.testTag("home_genre_chip_$genre")
                        )
                    }
                }
            }
        }

        // Continue Watching
        if (uiState.continueWatching.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MinimalSectionHeader(title = "Продолжить")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.continueWatching, key = { "cw_${it.id}" }) { movie ->
                            ContinueWatchingPillCard(
                                movie = movie,
                                onClick = { onPlayClick(movie.id) }
                            )
                        }
                    }
                }
            }
        }

        // HD & Open Cinema Selection
        if (uiState.hdSelections.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    MinimalSectionHeader(
                        title = "В высоком качестве",
                        subtitle = "Реальные фильмы и классика HD",
                        actionLabel = "Все",
                        onAction = onOpenSearch
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(uiState.hdSelections.take(10), key = { "hd_${it.id}" }) { movie ->
                            MoviePosterCard(
                                movie = movie,
                                onClick = { onMovieClick(movie.id) },
                                onToggleFavorite = { onToggleFavorite(movie.id) },
                                showRating = prefs.showRatingOnCards,
                                showMeta = prefs.showMetaOnCards,
                                showGenres = prefs.showGenresOnCards,
                                modifier = Modifier.width(148.dp)
                            )
                        }
                    }
                }
            }
        }

        // Personalized Recommendations
        if (uiState.recommendations.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Подобрано для вас",
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textPrimary
                    )
                    uiState.recommendations.take(4).forEach { rec ->
                        RecommendationCard(
                            item = rec,
                            onClick = { onMovieClick(rec.movie.id) },
                            onPlayClick = { onPlayClick(rec.movie.id) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Classic World Cinema
        if (uiState.classicCinema.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    MinimalSectionHeader(
                        title = "Золотая коллекция",
                        subtitle = "Шедевры мирового кино",
                        actionLabel = "Каталог",
                        onAction = onOpenSearch
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(uiState.classicCinema.take(10), key = { "classic_${it.id}" }) { movie ->
                            MoviePosterCard(
                                movie = movie,
                                onClick = { onMovieClick(movie.id) },
                                onToggleFavorite = { onToggleFavorite(movie.id) },
                                showRating = prefs.showRatingOnCards,
                                showMeta = prefs.showMetaOnCards,
                                showGenres = prefs.showGenresOnCards,
                                modifier = Modifier.width(148.dp)
                            )
                        }
                    }
                }
            }
        }

        // Downloaded for Offline
        if (uiState.downloadedMovies.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    MinimalSectionHeader(
                        title = "Доступно офлайн",
                        actionLabel = "Управлять",
                        onAction = onOpenOfflineTab
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(uiState.downloadedMovies, key = { "dl_${it.id}" }) { movie ->
                            MoviePosterCard(
                                movie = movie,
                                onClick = { onMovieClick(movie.id) },
                                onToggleFavorite = { onToggleFavorite(movie.id) },
                                showRating = prefs.showRatingOnCards,
                                showMeta = prefs.showMetaOnCards,
                                showGenres = false,
                                modifier = Modifier.width(142.dp)
                            )
                        }
                    }
                }
            }
        }

        // Live Archive.org Load More Card
        item {
            MinimalPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(palette.surfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isSyncingOnline) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = palette.accent
                            )
                        } else {
                            Icon(
                                imageVector = LucideIcons.Globe,
                                contentDescription = null,
                                tint = palette.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Онлайн-каталог Archive.org",
                            style = MaterialTheme.typography.titleSmall,
                            color = palette.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "В базе сейчас ${uiState.allMovies.size} фильмов. Загрузите ещё реальных картин.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary
                        )
                    }
                    MinimalSecondaryButton(
                        text = if (uiState.isSyncingOnline) "..." else "Ещё",
                        onClick = onSyncMoreOnline,
                        enabled = !uiState.isSyncingOnline && !uiState.effectiveOfflineMode,
                        modifier = Modifier.testTag("sync_archive_button")
                    )
                }
            }
        }
    }
}

@Composable
private fun MinimalOfflineBanner(
    downloadedCount: Int,
    isManualOffline: Boolean,
    onOpenOfflineTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalKinoPalette.current
    MinimalPanel(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = LucideIcons.WifiOff,
                contentDescription = null,
                tint = palette.accent,
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isManualOffline) "Автономный режим" else "Нет подключения",
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.textPrimary
                )
                Text(
                    text = "Кеш активен · Офлайн-фильмов: $downloadedCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
            }
            Text(
                text = "Открыть",
                style = MaterialTheme.typography.labelLarge,
                color = palette.accent,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onOpenOfflineTab)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun HeroCinemaCard(
    movie: MovieEntity,
    onOpenDetail: () -> Unit,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalKinoPalette.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(292.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(palette.surfaceElevated)
            .border(1.dp, palette.borderSubtle, RoundedCornerShape(22.dp))
            .bounceClick(scaleDown = 0.98f, onClick = onOpenDetail)
            .testTag("hero_showcase_card")
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
                            Color.Black.copy(alpha = 0.14f),
                            Color.Black.copy(alpha = 0.42f),
                            Color.Black.copy(alpha = 0.92f)
                        )
                    )
                )
        )

        // Subtle rating pill top-left
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = 0.56f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = LucideIcons.Star,
                contentDescription = null,
                tint = palette.ratingGold,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "${movie.rating}  ·  ${movie.releaseYear}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${movie.genres.take(3).joinToString(" · ")} · ${movie.durationMinutes} мин",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.78f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MinimalPrimaryButton(
                    text = if (movie.isDownloaded) "Офлайн" else "Смотреть",
                    icon = LucideIcons.Play,
                    onClick = onPlay,
                    useAccent = false,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hero_play_button")
                )
                MinimalIconButton(
                    icon = if (movie.isFavorite) LucideIcons.BookmarkFilled else LucideIcons.Bookmark,
                    contentDescription = "В избранное",
                    onClick = onToggleFavorite,
                    containerColor = Color.White.copy(alpha = 0.16f),
                    contentColor = if (movie.isFavorite) palette.accent else Color.White,
                    borderColor = Color.White.copy(alpha = 0.2f),
                    size = 48.dp,
                    iconSize = 19.dp,
                    modifier = Modifier.testTag("hero_fav_button")
                )
            }
        }
    }
}

@Composable
private fun ContinueWatchingPillCard(
    movie: MovieEntity,
    onClick: () -> Unit
) {
    val palette = LocalKinoPalette.current
    MinimalPanel(
        modifier = Modifier
            .width(236.dp)
            .bounceClick(scaleDown = 0.97f, onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.surfaceElevated)
            ) {
                MovieArtworkImage(
                    posterKey = movie.posterKey,
                    posterUrl = movie.posterUrl,
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val mins = movie.watchProgressSeconds / 60
                val secs = movie.watchProgressSeconds % 60
                Text(
                    text = "С %02d:%02d".format(mins, secs),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.accent
                )
            }
            Icon(
                imageVector = LucideIcons.Play,
                contentDescription = "Продолжить",
                tint = palette.textPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
