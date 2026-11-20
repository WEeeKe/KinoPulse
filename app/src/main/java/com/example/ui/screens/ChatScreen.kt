package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.local.ChatMessageEntity
import com.example.data.local.MovieEntity
import com.example.ui.components.MinimalIconButton
import com.example.ui.components.MinimalPanel
import com.example.ui.components.MinimalPill
import com.example.ui.components.bounceClick
import com.example.ui.icons.LucideIcons
import com.example.ui.theme.LocalKinoPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    selectedRoomId: String,
    movies: List<MovieEntity>,
    messages: List<ChatMessageEntity>,
    onSelectRoom: (String) -> Unit,
    onSendMessage: (String, ChatMessageEntity?) -> Unit,
    onToggleLike: (String) -> Unit,
    bottomDockClearance: Dp = 92.dp,
    modifier: Modifier = Modifier
) {
    val palette = LocalKinoPalette.current
    var messageInput by remember { mutableStateOf("") }
    var replyTarget by remember { mutableStateOf<ChatMessageEntity?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .imePadding()
            .padding(bottom = bottomDockClearance)
    ) {
        // Minimalist Room Selector
        LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                MinimalPill(
                    text = "Общий киноклуб",
                    selected = selectedRoomId == "global",
                    onClick = { onSelectRoom("global") },
                    modifier = Modifier.testTag("chat_room_global")
                )
            }
            items(movies.take(8), key = { it.id }) { movie ->
                MinimalPill(
                    text = movie.title,
                    selected = selectedRoomId == movie.id,
                    onClick = { onSelectRoom(movie.id) }
                )
            }
        }

        // Subtle local-mode notice
        MinimalPanel(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = LucideIcons.Info,
                    contentDescription = null,
                    tint = palette.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Локальный киноклуб: ваши реплики и заметки сохраняются на устройстве.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.MessageCircle,
                            contentDescription = null,
                            tint = palette.textTertiary,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Комната обсуждения открыта",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "Оставьте первый отзыв или поделитесь впечатлениями о фильме",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary
                        )
                    }
                }
            } else {
                items(messages, key = { it.id }) { msg ->
                    ChatMessageBubble(
                        message = msg,
                        onReply = { replyTarget = msg },
                        onToggleLike = { onToggleLike(msg.id) }
                    )
                }
            }
        }

        // Reply Preview Strip
        AnimatedVisibility(
            visible = replyTarget != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            replyTarget?.let { target ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.surfaceElevated)
                        .border(1.dp, palette.borderSubtle, RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ответ для ${target.senderName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.accent,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = target.text,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    MinimalIconButton(
                        icon = LucideIcons.X,
                        contentDescription = "Отменить ответ",
                        onClick = { replyTarget = null },
                        containerColor = Color.Transparent,
                        borderColor = Color.Transparent,
                        size = 34.dp,
                        iconSize = 16.dp
                    )
                }
            }
        }

        // Minimalist Composer Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(palette.surface)
                .border(1.dp, palette.border, RoundedCornerShape(24.dp))
                .padding(start = 16.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (messageInput.isEmpty()) {
                    Text(
                        text = "Комментарий о фильме…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textTertiary
                    )
                }
                BasicTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    maxLines = 3,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = palette.textPrimary),
                    cursorBrush = SolidColor(palette.accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chat_message_input")
                )
            }

            val canSend = messageInput.isNotBlank()
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .testTag("chat_send_button"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (canSend) palette.accent else palette.surfaceElevated)
                        .bounceClick(scaleDown = 0.9f, enabled = canSend) {
                            onSendMessage(messageInput, replyTarget)
                            messageInput = ""
                            replyTarget = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LucideIcons.Send,
                        contentDescription = "Отправить",
                        tint = if (canSend) palette.onAccent else palette.textTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessageEntity,
    onReply: () -> Unit,
    onToggleLike: () -> Unit
) {
    val palette = LocalKinoPalette.current
    val bubbleBg = if (message.isMine) palette.surfaceElevated else palette.surface
    val borderColor = if (message.isMine) palette.accent.copy(alpha = 0.35f) else palette.borderSubtle

    val likeScale by animateFloatAsState(
        targetValue = if (message.isLikedByMe) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "chat_like_scale"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .clip(RoundedCornerShape(18.dp))
                .background(bubbleBg)
                .border(1.dp, borderColor, RoundedCornerShape(18.dp))
                .clickable(onClick = onReply)
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textPrimary
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(palette.background.copy(alpha = 0.7f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = message.senderBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (message.isMine) palette.accent else palette.textSecondary
                        )
                    }
                }
                Text(
                    text = formatChatTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textTertiary
                )
            }

            if (!message.replyToSender.isNullOrBlank() && !message.replyToSnippet.isNullOrBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.background.copy(alpha = 0.65f))
                        .border(1.dp, palette.borderSubtle, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = message.replyToSender,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.accent,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = message.replyToSnippet,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onReply)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.Reply,
                        contentDescription = "Ответить",
                        tint = palette.textTertiary,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onToggleLike)
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (message.isLikedByMe) LucideIcons.HeartFilled else LucideIcons.Heart,
                        contentDescription = "Нравится",
                        tint = if (message.isLikedByMe) palette.accent else palette.textTertiary,
                        modifier = Modifier
                            .size(15.dp)
                            .graphicsLayer {
                                scaleX = likeScale
                                scaleY = likeScale
                            }
                    )
                    if (message.likesCount > 0) {
                        Text(
                            text = message.likesCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (message.isLikedByMe) palette.accent else palette.textSecondary
                        )
                    }
                }
            }
        }
    }
}

private fun formatChatTime(ts: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.forLanguageTag("ru"))
    return sdf.format(Date(ts))
}
