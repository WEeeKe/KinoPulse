package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.prefs.CardLayoutMode
import com.example.notifications.NewReleaseNotificationHelper
import com.example.ui.MainUiState
import com.example.ui.components.MinimalPanel
import com.example.ui.components.MinimalPrimaryButton
import com.example.ui.components.MinimalSecondaryButton
import com.example.ui.components.MinimalSegmentedBar
import com.example.ui.components.MinimalSwitchRow
import com.example.ui.icons.LucideIcons
import com.example.ui.theme.LocalKinoPalette
import com.example.ui.theme.ThemeMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    uiState: MainUiState,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetAmoledNight: (Boolean) -> Unit,
    onSetCardLayoutMode: (CardLayoutMode) -> Unit,
    onSetShowRatingOnCards: (Boolean) -> Unit,
    onSetShowMetaOnCards: (Boolean) -> Unit,
    onSetShowGenresOnCards: (Boolean) -> Unit,
    onSetOfflineMode: (Boolean) -> Unit,
    onSetNotificationsEnabled: (Boolean) -> Unit,
    onTriggerNotification: () -> Unit,
    onSyncMoreOnline: () -> Unit,
    onClearVolatileCache: () -> Unit,
    onSaveDisplayName: (String) -> Unit,
    onExportSyncCode: ((String) -> Unit) -> Unit,
    onImportSyncCode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalKinoPalette.current
    val context = LocalContext.current
    val prefs = uiState.preferences

    var nameInput by remember(prefs.userDisplayName) {
        mutableStateOf(prefs.userDisplayName)
    }
    var showImportDialog by remember { mutableStateOf(false) }
    var importCodeInput by remember { mutableStateOf("") }
    var exportedCodePreview by remember { mutableStateOf<String?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onSetNotificationsEnabled(granted)
        if (granted) {
            onTriggerNotification()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile
        item {
            MinimalPanel(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingsSectionTitle(
                        icon = LucideIcons.User,
                        title = "Профиль",
                        subtitle = "Устройство: ${prefs.deviceId}"
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(13.dp))
                                .background(palette.surfaceElevated)
                                .border(1.dp, palette.borderSubtle, RoundedCornerShape(13.dp))
                                .padding(horizontal = 14.dp, vertical = 13.dp)
                        ) {
                            if (nameInput.isEmpty()) {
                                Text(
                                    text = "Имя в киноклубе",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.textTertiary
                                )
                            }
                            BasicTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = palette.textPrimary),
                                cursorBrush = SolidColor(palette.accent),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        MinimalPrimaryButton(
                            text = "Сохранить",
                            onClick = { onSaveDisplayName(nameInput) }
                        )
                    }
                }
            }
        }

        // Theme & Night Mode
        item {
            MinimalPanel(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingsSectionTitle(
                        icon = LucideIcons.Moon,
                        title = "Оформление",
                        subtitle = "Минималистичная палитра без Material You"
                    )

                    val themeModes = listOf(ThemeMode.NIGHT, ThemeMode.DAY, ThemeMode.SYSTEM)
                    val themeLabels = listOf("Ночной", "Дневной", "Авто")
                    MinimalSegmentedBar(
                        options = themeLabels,
                        selectedIndex = themeModes.indexOf(prefs.themeMode).coerceAtLeast(0),
                        onSelect = { idx -> onSetThemeMode(themeModes[idx]) },
                        modifier = Modifier.testTag("theme_mode_selector")
                    )

                    MinimalSwitchRow(
                        title = "AMOLED чистый чёрный",
                        subtitle = "Глубокий #000000 фон для комфортного просмотра ночью",
                        checked = prefs.amoledNightMode,
                        onCheckedChange = onSetAmoledNight
                    )
                }
            }
        }

        // Card Display Settings
        item {
            MinimalPanel(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingsSectionTitle(
                        icon = LucideIcons.LayoutGrid,
                        title = "Отображение карточек",
                        subtitle = "Настройте плотность сетки и подписи в каталоге"
                    )

                    val layouts = listOf(CardLayoutMode.GRID_2, CardLayoutMode.GRID_3, CardLayoutMode.LIST)
                    MinimalSegmentedBar(
                        options = listOf("2 в ряд", "3 в ряд", "Список"),
                        selectedIndex = layouts.indexOf(prefs.cardLayoutMode).coerceAtLeast(0),
                        onSelect = { idx -> onSetCardLayoutMode(layouts[idx]) }
                    )

                    HorizontalDivider(color = palette.borderSubtle)

                    MinimalSwitchRow(
                        title = "Показывать рейтинг",
                        subtitle = "Оценка зрителей в углу постера",
                        checked = prefs.showRatingOnCards,
                        onCheckedChange = onSetShowRatingOnCards
                    )
                    MinimalSwitchRow(
                        title = "Год и длительность",
                        subtitle = "Краткие метаданные под названием",
                        checked = prefs.showMetaOnCards,
                        onCheckedChange = onSetShowMetaOnCards
                    )
                    MinimalSwitchRow(
                        title = "Жанры на карточке",
                        subtitle = "Категории фильма в каталоге и списке",
                        checked = prefs.showGenresOnCards,
                        onCheckedChange = onSetShowGenresOnCards
                    )
                }
            }
        }

        // Real Catalog & Offline Cache
        item {
            MinimalPanel(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingsSectionTitle(
                        icon = LucideIcons.WifiOff,
                        title = "Каталог и офлайн-кеш",
                        subtitle = "В базе: ${uiState.allMovies.size} фильмов · ${if (uiState.isDeviceOnline) "Сеть доступна" else "Нет сети"}"
                    )

                    MinimalSwitchRow(
                        title = "Автономный офлайн-режим",
                        subtitle = "Воспроизводить только скачанные копии и локальный кеш",
                        checked = prefs.offlineModeEnabled,
                        onCheckedChange = onSetOfflineMode,
                        modifier = Modifier.testTag("offline_mode_switch")
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(13.dp))
                            .background(palette.surfaceElevated)
                            .border(1.dp, palette.borderSubtle, RoundedCornerShape(13.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Офлайн-фильмы: ${uiState.cacheStats.offlineVideosCount} (${String.format("%.1f", uiState.cacheStats.offlineVideosSizeMb)} МБ)",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "Кеш постеров и сети: ${String.format("%.2f", uiState.cacheStats.imageCacheSizeMb + uiState.cacheStats.httpCacheSizeMb)} МБ",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.isSyncingOnline) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(palette.surfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = palette.accent
                                )
                            }
                        } else {
                            MinimalPrimaryButton(
                                text = "Пополнить каталог",
                                icon = LucideIcons.Globe,
                                onClick = onSyncMoreOnline,
                                enabled = !uiState.effectiveOfflineMode,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        MinimalSecondaryButton(
                            text = "Кеш",
                            icon = LucideIcons.Trash,
                            onClick = onClearVolatileCache
                        )
                    }
                }
            }
        }

        // Notifications
        item {
            MinimalPanel(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingsSectionTitle(
                        icon = LucideIcons.Bell,
                        title = "Уведомления о новинках",
                        subtitle = "Фоновая проверка новых релизов каждые 12 часов"
                    )

                    MinimalSwitchRow(
                        title = "Оповещать о премьерах",
                        checked = prefs.notificationsEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !NewReleaseNotificationHelper.hasNotificationPermission(context)
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onSetNotificationsEnabled(enabled)
                            }
                        },
                        modifier = Modifier.testTag("notifications_switch")
                    )

                    MinimalSecondaryButton(
                        text = "Проверить новинки сейчас",
                        icon = LucideIcons.Sparkles,
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !NewReleaseNotificationHelper.hasNotificationPermission(context)
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onTriggerNotification()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trigger_notification_button")
                    )
                }
            }
        }

        // Cross-device Sync
        item {
            MinimalPanel(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingsSectionTitle(
                        icon = LucideIcons.RefreshCw,
                        title = "Синхронизация устройств",
                        subtitle = "Перенос избранного, прогресса и настроек по коду KP1"
                    )

                    if (prefs.lastSyncTimestamp > 0L) {
                        val dateText = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru"))
                            .format(Date(prefs.lastSyncTimestamp))
                        Text(
                            text = "Последний снимок: $dateText",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.accent
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MinimalPrimaryButton(
                            text = "Экспорт",
                            icon = LucideIcons.Share,
                            onClick = {
                                onExportSyncCode { code -> exportedCodePreview = code }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        MinimalSecondaryButton(
                            text = "Импорт",
                            icon = LucideIcons.Download,
                            onClick = { showImportDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    exportedCodePreview?.let { code ->
        AlertDialog(
            onDismissRequest = { exportedCodePreview = null },
            containerColor = palette.surface,
            title = {
                Text(
                    text = "Код синхронизации готов",
                    color = palette.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Скопируйте код или отправьте его на второе устройство:",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(palette.surfaceElevated)
                            .border(1.dp, palette.borderSubtle, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = code.take(140) + if (code.length > 140) "…" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textPrimary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("KinoPulse Sync", code))
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, code)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Перенести профиль KinoPulse"))
                        exportedCodePreview = null
                    }
                ) {
                    Icon(
                        imageVector = LucideIcons.Share,
                        contentDescription = null,
                        tint = palette.accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Поделиться", color = palette.accent)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("KinoPulse Sync", code))
                        exportedCodePreview = null
                    }
                ) {
                    Icon(
                        imageVector = LucideIcons.Copy,
                        contentDescription = null,
                        tint = palette.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Копировать", color = palette.textPrimary)
                }
            }
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = palette.surface,
            title = {
                Text(
                    text = "Импорт профиля",
                    color = palette.textPrimary
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.surfaceElevated)
                        .border(1.dp, palette.border, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    if (importCodeInput.isEmpty()) {
                        Text(
                            text = "Вставьте код KP1:…",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textTertiary
                        )
                    }
                    BasicTextField(
                        value = importCodeInput,
                        onValueChange = { importCodeInput = it },
                        maxLines = 4,
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = palette.textPrimary),
                        cursorBrush = SolidColor(palette.accent),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onImportSyncCode(importCodeInput)
                        importCodeInput = ""
                        showImportDialog = false
                    }
                ) {
                    Text("Синхронизировать", color = palette.accent, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Отмена", color = palette.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionTitle(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    val palette = LocalKinoPalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(palette.surfaceElevated)
                .border(1.dp, palette.borderSubtle, RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = palette.textPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = palette.textSecondary
            )
        }
    }
}
