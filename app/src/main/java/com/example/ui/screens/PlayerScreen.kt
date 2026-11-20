package com.example.ui.screens

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.data.local.MovieEntity
import kotlinx.coroutines.delay
import java.io.File

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    movie: MovieEntity,
    isOfflineMode: Boolean,
    onSaveProgress: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localFile = movie.localVideoPath?.let { File(it) }?.takeIf { it.exists() }
    val playingOfflineFile = movie.isDownloaded && localFile != null

    var nightDimmer by remember { mutableFloatStateOf(0f) }
    var playerError by remember { mutableStateOf<String?>(null) }

    val canStartPlayback = playingOfflineFile || !isOfflineMode

    val exoPlayer = remember(movie.id, playingOfflineFile) {
        if (!canStartPlayback) return@remember null
        ExoPlayer.Builder(context).build().apply {
            val uri = if (playingOfflineFile) {
                Uri.fromFile(localFile)
            } else {
                Uri.parse(movie.videoUrl)
            }
            setMediaItem(MediaItem.fromUri(uri))
            if (movie.watchProgressSeconds > 0L) {
                seekTo(movie.watchProgressSeconds * 1000L)
            }
            playWhenReady = true
            prepare()
        }
    }

    if (exoPlayer != null) {
        DisposableEffect(exoPlayer) {
            val listener = object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    playerError = "Не удалось воспроизвести поток. Проверьте сеть или загрузите фильм для офлайн."
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Player Top Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("player_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = movie.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = if (playingOfflineFile) "Офлайн-файл на устройстве" else "Потоковое воспроизведение",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (playingOfflineFile) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                if (playingOfflineFile) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DownloadDone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Офлайн",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Video Surface
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (!canStartPlayback || playerError != null) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .background(Color(0xFF161A26), RoundedCornerShape(20.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = playerError ?: "Фильм не загружен для офлайн-режима",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Отключите офлайн-режим или скачайте фильм на экран устройства при наличии интернета.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                        Button(onClick = onBack) {
                            Text("Вернуться к фильму")
                        }
                    }
                } else if (exoPlayer != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = exoPlayer
                                    useController = true
                                    layoutParams = FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        if (nightDimmer > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = nightDimmer * 0.65f))
                            )
                        }
                    }
                }
            }

            // Night Comfort Dimmer Bar
            Surface(
                color = Color(0xFF121520),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.BrightnessLow,
                        contentDescription = "Ночное затемнение",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Ночной фильтр",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                    Slider(
                        value = nightDimmer,
                        onValueChange = { nightDimmer = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
