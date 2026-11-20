package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.R
import com.example.data.local.MovieEntity
import com.example.data.recommendations.RecommendedMovie
import com.example.ui.icons.LucideIcons
import com.example.ui.theme.LocalKinoPalette

@DrawableRes
fun posterDrawableForKey(key: String): Int {
    return when (key) {
        "hero_cinema" -> R.drawable.img_hero_cinema
        "horizon" -> R.drawable.img_poster_horizon
        "neon_rain" -> R.drawable.img_poster_neon_rain
        "alpine_echo" -> R.drawable.img_poster_alpine_echo
        else -> R.drawable.img_poster_horizon
    }
}

@Composable
fun MovieArtworkImage(
    posterKey: String,
    posterUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val fallbackRes = posterDrawableForKey(posterKey)
    if (posterUrl.isBlank()) {
        Image(
            painter = painterResource(id = fallbackRes),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        val context = LocalContext.current
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(posterUrl)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .placeholder(fallbackRes)
                .error(fallbackRes)
                .crossfade(220)
                .build(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

@Composable
fun MoviePosterCard(
    movie: MovieEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    showRating: Boolean = true,
    showMeta: Boolean = true,
    showGenres: Boolean = true
) {
    val palette = LocalKinoPalette.current
    val favScale by animateFloatAsState(
        targetValue = if (movie.isFavorite) 1.12f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fav_pop"
    )

    Column(
        modifier = modifier
            .bounceClick(scaleDown = 0.96f, onClick = onClick)
            .testTag("movie_card_${movie.id}"),
        verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.68f)
                .clip(RoundedCornerShape(if (compact) 12.dp else 16.dp))
                .background(palette.surfaceElevated)
                .border(1.dp, palette.borderSubtle, RoundedCornerShape(if (compact) 12.dp else 16.dp))
        ) {
            MovieArtworkImage(
                posterKey = movie.posterKey,
                posterUrl = movie.posterUrl,
                contentDescription = "Постер фильма ${movie.title}",
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.22f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.55f)
                            )
                        )
                    )
            )

            // Top-left minimal rating badge
            if (showRating) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(if (compact) 6.dp else 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = if (compact) 5.dp else 7.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.Star,
                        contentDescription = null,
                        tint = palette.ratingGold,
                        modifier = Modifier.size(if (compact) 10.dp else 12.dp)
                    )
                    Text(
                        text = movie.rating.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Top-right bookmark button
            if (!compact) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.52f))
                        .clickable(onClick = onToggleFavorite)
                        .testTag("fav_button_${movie.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (movie.isFavorite) LucideIcons.BookmarkFilled else LucideIcons.Bookmark,
                        contentDescription = if (movie.isFavorite) "Убрать из избранного" else "В избранное",
                        tint = if (movie.isFavorite) palette.accent else Color.White,
                        modifier = Modifier
                            .size(16.dp)
                            .graphicsLayer {
                                scaleX = favScale
                                scaleY = favScale
                            }
                    )
                }
            }

            // Bottom subtle offline/HD indicator
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(if (compact) 6.dp else 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (movie.isHd && !compact) {
                    Text(
                        text = "HD",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.55f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (movie.isDownloaded) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(palette.onlineGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.Check,
                            contentDescription = "Доступно офлайн",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = movie.title,
                style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
                color = palette.textPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (showMeta) {
                Text(
                    text = if (compact) "${movie.releaseYear}" else "${movie.releaseYear} · ${movie.durationMinutes} мин",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary,
                    maxLines = 1
                )
            }
            if (showGenres && !compact) {
                Text(
                    text = movie.genres.take(2).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MovieListRowCard(
    movie: MovieEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    showRating: Boolean = true,
    showMeta: Boolean = true,
    showGenres: Boolean = true
) {
    val palette = LocalKinoPalette.current
    MinimalPanel(
        modifier = modifier
            .fillMaxWidth()
            .bounceClick(scaleDown = 0.98f, onClick = onClick)
            .testTag("movie_card_${movie.id}"),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(74.dp)
                    .height(106.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.surfaceElevated)
            ) {
                MovieArtworkImage(
                    posterKey = movie.posterKey,
                    posterUrl = movie.posterUrl,
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize()
                )
                if (movie.isDownloaded) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(5.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(palette.onlineGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.Check,
                            contentDescription = "Офлайн",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (showRating) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = LucideIcons.Star,
                                contentDescription = null,
                                tint = palette.ratingGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = movie.rating.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (showMeta) {
                    Text(
                        text = "${movie.releaseYear} · ${movie.durationMinutes} мин · ${movie.director}",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (showGenres) {
                    Text(
                        text = movie.genres.joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = movie.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textTertiary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            MinimalIconButton(
                icon = if (movie.isFavorite) LucideIcons.BookmarkFilled else LucideIcons.Bookmark,
                contentDescription = if (movie.isFavorite) "Убрать из избранного" else "В избранное",
                onClick = onToggleFavorite,
                contentColor = if (movie.isFavorite) palette.accent else palette.textSecondary,
                containerColor = palette.surfaceElevated,
                size = 38.dp,
                iconSize = 17.dp,
                modifier = Modifier.testTag("fav_button_${movie.id}")
            )
        }
    }
}

@Composable
fun RecommendationCard(
    item: RecommendedMovie,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalKinoPalette.current
    val movie = item.movie
    MinimalPanel(
        modifier = modifier
            .bounceClick(scaleDown = 0.98f, onClick = onClick)
            .testTag("rec_card_${movie.id}"),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(78.dp)
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.surfaceElevated)
            ) {
                MovieArtworkImage(
                    posterKey = movie.posterKey,
                    posterUrl = movie.posterUrl,
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(5.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(palette.accent)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${item.matchPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

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
                    text = item.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.Star,
                        contentDescription = null,
                        tint = palette.ratingGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${movie.rating} · ${movie.releaseYear} · ${movie.durationMinutes} мин",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textTertiary
                    )
                }
            }

            MinimalIconButton(
                icon = LucideIcons.Play,
                contentDescription = "Смотреть ${movie.title}",
                onClick = onPlayClick,
                containerColor = palette.ctaBackground,
                contentColor = palette.ctaContent,
                borderColor = Color.Transparent,
                size = 40.dp,
                iconSize = 16.dp
            )
        }
    }
}
