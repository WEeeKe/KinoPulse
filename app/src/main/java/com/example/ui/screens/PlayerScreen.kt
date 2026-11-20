package com.example.ui.screens

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.local.MovieEntity
import com.example.ui.components.MinimalIconButton
import com.example.ui.components.MinimalPrimaryButton
import com.example.ui.components.MinimalSecondaryButton
import com.example.ui.components.bounceClick
import com.example.ui.icons.LucideIcons
import com.example.ui.theme.CinemaAccentRed
import com.example.ui.theme.SignalEmerald
import kotlinx.coroutines.delay
import java.io.File
import kotlin.math.roundToInt

private val PlaybackSpeeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    movie: MovieEntity,
    isOfflineMode: Boolean,
    resolveVideoUrl: suspend (MovieEntity) -> String?,
    onSaveProgress: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localFile = remember(movie.localVideoPath) {
        movie.localVideoPath?.let { File(it) }?.takeIf { it.exists() }
    }
    val playingOfflineFile = movie.isDownloaded && localFile != null

    var nightDimmer by remember { mutableFloatStateOf(0f) }
    var speedIndex by remember { mutableIntStateOf(1) } // 1.0x
    var zoomToFill by remember { mutableStateOf(false) }
    var retryToken by remember { mutableIntStateOf(0) }

    var isResolvingStream by remember { mutableStateOf(false) }
    var resolvedUri by remember { mutableStateOf<Uri?>(null) }
    var playerError by remember { mutableStateOf<String?>(null) }

    val canAttemptPlayback = playingOfflineFile || !isOfflineMode

    LaunchedEffect(movie.id, playingOfflineFile, isOfflineMode, retryToken) {
        playerError = null
        resolvedUri = null
        if (playingOfflineFile && localFile != null) {
            isResolvingStream = false
            resolvedUri = Uri.fromFile(localFile)
        } else if (isOfflineMode) {
            isResolvingStream = false
            playerError = "Фильм не загружен для офлайн-режима"
        } else {
            isResolvingStream = true
            val url = resolveVideoUrl(movie)
            isResolvingStream = false
            if (!url.isNullOrBlank()) {
                resolvedUri = Uri.parse(url)
            } else {
                playerError = "Не удалось получить видеопоток из Internet Archive. Проверьте подключение и попробуйте снова."
            }
        }
    }

    val exoPlayer = remember(movie.id, resolvedUri) {
        val uri = resolvedUri ?: return@remember null
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            if (movie.watchProgressSeconds > 0L) {
                seekTo(movie.watchProgressSeconds * 1000L)
            }
            playbackParameters = PlaybackParameters(PlaybackSpeeds[speedIndex])
            playWhenReady = true
            prepare()
        }
    }

    LaunchedEffect(exoPlayer, speedIndex) {
        exoPlayer?.playbackParameters = PlaybackParameters(PlaybackSpeeds[speedIndex])
    }

    if (exoPlayer != null) {
        DisposableEffect(exoPlayer) {
            val listener = object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    playerError = "Ошибка воспроизведения потока. Проверьте интернет или повторите попытку."
                }
            }
            exoPlayer.addListener(listener)
            onDispose {
                val posSeconds = (exoPlayer.currentPosition / 1000L).coerceAtLeast(0L)
                onSaveProgress(posSeconds)
                exoPlayer.removeListener(listener)
                exoPlayer.release()
            }
        }

        LaunchedEffect(exoPlayer) {
            while (true) {
                delay(5000L)
                if (exoPlayer.isPlaying) {
                    onSaveProgress((exoPlayer.currentPosition / 1000L).coerceAtLeast(0L))
                }
            }
        }
    }

    val currentSpeed = PlaybackSpeeds[speedIndex]
    val speedLabel = if (currentSpeed == 1.0f) "1x" else "${currentSpeed}x"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Minimalist Dark Cinema Top Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MinimalIconButton(
                    icon = LucideIcons.ChevronLeft,
                    contentDescription = "Назад",
                    onClick = onBack,
                    containerColor = Color(0xFF16161D),
                    contentColor = Color.White,
                    borderColor = Color(0xFF282834),
                    modifier = Modifier.testTag("player_back_button")
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = when {
                                playingOfflineFile -> "Офлайн-копия"
                                movie.archiveId.isNotBlank() -> "Internet Archive · ${movie.releaseYear}"
                                else -> "Поток · ${movie.releaseYear}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (playingOfflineFile) SignalEmerald else Color.White.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (movie.isHd) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.14f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "HD",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Speed Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF16161D))
                        .border(1.dp, Color(0xFF282834), RoundedCornerShape(50))
                        .bounceClick(scaleDown = 0.94f) {
                            speedIndex = (speedIndex + 1) % PlaybackSpeeds.size
                        }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.Gauge,
                        contentDescription = "Скорость воспроизведения",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = speedLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Aspect Ratio Toggle
                MinimalIconButton(
                    icon = LucideIcons.Maximize,
                    contentDescription = if (zoomToFill) "По размеру экрана" else "Заполнить экран",
                    onClick = { zoomToFill = !zoomToFill },
                    containerColor = if (zoomToFill) Color.White else Color(0xFF16161D),
                    contentColor = if (zoomToFill) Color.Black else Color.White,
                    borderColor = Color(0xFF282834),
                    size = 40.dp,
                    iconSize = 18.dp
                )
            }

            // Video Stage
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isResolvingStream -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            CircularProgressIndicator(
                                color = CinemaAccentRed,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Подбираем оптимальный видеопоток…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                text = movie.originalTitle.ifBlank { movie.title },
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.55f)
                            )
                        }
                    }

                    !canAttemptPlayback || playerError != null -> {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0xFF13131A))
                                .border(1.dp, Color(0xFF262632), RoundedCornerShape(22.dp))
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = LucideIcons.WifiOff,
                                contentDescription = null,
                                tint = CinemaAccentRed,
                                modifier = Modifier.size(38.dp)
                            )
                            Text(
                                text = playerError ?: "Фильм недоступен офлайн",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (isOfflineMode) {
                                    "Отключите офлайн-режим или скачайте фильм на устройство при наличии интернета."
                                } else {
                                    "Попробуйте перезагрузить поток или выберите другой фильм из каталога."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.68f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (!isOfflineMode) {
                                    MinimalPrimaryButton(
                                        text = "Повторить",
                                        icon = LucideIcons.RefreshCw,
                                        onClick = { retryToken++ },
                                        useAccent = true
                                    )
                                }
                                MinimalSecondaryButton(
                                    text = "Назад",
                                    onClick = onBack
                                )
                            }
                        }
                    }

                    exoPlayer != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (zoomToFill) Modifier.fillMaxSize()
                                    else Modifier.aspectRatio(16f / 9f)
                                )
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    PlayerView(ctx).apply {
                                        player = exoPlayer
                                        useController = true
                                        resizeMode = if (zoomToFill) {
                                            AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                        } else {
                                            AspectRatioFrameLayout.RESIZE_MODE_FIT
                                        }
                                        layoutParams = FrameLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                    }
                                },
                                update = { view ->
                                    view.player = exoPlayer
                                    view.resizeMode = if (zoomToFill) {
                                        AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                    } else {
                                        AspectRatioFrameLayout.RESIZE_MODE_FIT
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            androidx.compose.animation.AnimatedVisibility(
                                visible = nightDimmer > 0.01f,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = nightDimmer * 0.65f))
                                )
                            }
                        }
                    }
                }
            }

            // Minimalist Night Comfort Dimmer Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF13131A))
                    .border(1.dp, Color(0xFF23232E), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = LucideIcons.Moon,
                    contentDescription = "Ночное затемнение",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Ночной фильтр",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Slider(
                    value = nightDimmer,
                    onValueChange = { nightDimmer = it },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = CinemaAccentRed,
                        inactiveTrackColor = Color(0xFF2C2C38)
                    )
                )
                Text(
                    text = "${(nightDimmer * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }
        }
    }
}
