package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.prefs.CardLayoutMode
import com.example.ui.DurationFilter
import com.example.ui.MainUiState
import com.example.ui.MinRatingFilter
import com.example.ui.SortOption
import com.example.ui.YearFilterPreset
import com.example.ui.components.MinimalIconButton
import com.example.ui.components.MinimalPanel
import com.example.ui.components.MinimalPill
import com.example.ui.components.MinimalPrimaryButton
import com.example.ui.components.MinimalSecondaryButton
import com.example.ui.components.MinimalSegmentedBar
import com.example.ui.components.MinimalSwitchRow
import com.example.ui.components.MovieListRowCard
import com.example.ui.components.MoviePosterCard
import com.example.ui.icons.LucideIcons
import com.example.ui.theme.LocalKinoPalette
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    uiState: MainUiState,
    onQueryChange: (String) -> Unit,
    onToggleGenre: (String) -> Unit,
    onSelectYearPreset: (YearFilterPreset) -> Unit,
    onCustomYearRange: (Int, Int) -> Unit,
    onSelectMinRating: (MinRatingFilter) -> Unit,
    onSelectDuration: (DurationFilter) -> Unit,
    onSelectSort: (SortOption) -> Unit,
    onToggleOfflineOnly: () -> Unit,
    onToggleFavoritesOnly: () -> Unit,
    onToggleHdOnly: () -> Unit,
    onResetFilters: () -> Unit,
    onResetAll: () -> Unit,
    onSelectCardLayout: (CardLayoutMode) -> Unit,
    onSetShowRating: (Boolean) -> Unit,
    onSetShowMeta: (Boolean) -> Unit,
    onSetShowGenres: (Boolean) -> Unit,
    onSearchOnlineArchive: () -> Unit,
    onMovieClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalKinoPalette.current
    val filter = uiState.searchFilter
    val prefs = uiState.preferences

    var showFiltersSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }
    var showViewSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        // Minimalist Pill Search Field
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.border, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = LucideIcons.Search,
                    contentDescription = null,
                    tint = palette.textTertiary,
                    modifier = Modifier.size(18.dp)
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (filter.query.isEmpty()) {
                        Text(
                            text = "Фильм, режиссёр, актёр или год...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textTertiary
                        )
                    }
                    BasicTextField(
                        value = filter.query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = palette.textPrimary),
                        cursorBrush = SolidColor(palette.accent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_input")
                    )
                }
                if (filter.query.isNotEmpty()) {
                    Icon(
                        imageVector = LucideIcons.X,
                        contentDescription = "Очистить",
                        tint = palette.textSecondary,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onQueryChange("") }
                    )
                }
            }

            // Compact Non-Cluttered Toolbar: Filters · Sort · Card Layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MinimalPill(
                    text = "Фильтры",
                    selected = filter.activeFilterCount > 0,
                    leadingIcon = LucideIcons.SlidersHorizontal,
                    badgeCount = filter.activeFilterCount,
                    onClick = { showFiltersSheet = true },
                    modifier = Modifier.testTag("open_filters_button")
                )

                MinimalPill(
                    text = filter.sortBy.label,
                    selected = false,
                    leadingIcon = LucideIcons.ArrowUpDown,
                    onClick = { showSortSheet = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_sort_button")
                )

                val layoutIcon = when (prefs.cardLayoutMode) {
                    CardLayoutMode.GRID_2 -> LucideIcons.LayoutGrid
                    CardLayoutMode.GRID_3 -> LucideIcons.Grid3x3
                    CardLayoutMode.LIST -> LucideIcons.ListIcon
                }
                MinimalIconButton(
                    icon = layoutIcon,
                    contentDescription = "Вид карточек",
                    onClick = { showViewSheet = true },
                    size = 38.dp,
                    iconSize = 18.dp,
                    modifier = Modifier.testTag("open_view_settings_button")
                )
            }

            // Active Filter Chips (only visible when filters are active -> keeps UI super clean!)
            AnimatedVisibility(visible = filter.activeFilterCount > 0 || filter.query.length >= 2) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (filter.query.length >= 2 && !uiState.effectiveOfflineMode) {
                        item {
                            MinimalPill(
                                text = if (uiState.isSyncingOnline) "Поиск в Archive..." else "Искать в Archive.org",
                                selected = false,
                                leadingIcon = LucideIcons.Globe,
                                onClick = onSearchOnlineArchive
                            )
                        }
                    }
                    items(filter.selectedGenres.toList()) { genre ->
                        MinimalPill(
                            text = genre,
                            selected = true,
                            leadingIcon = LucideIcons.X,
                            onClick = { onToggleGenre(genre) }
                        )
                    }
                    if (filter.yearPreset != YearFilterPreset.ALL) {
                        item {
                            MinimalPill(
                                text = "${filter.yearStart}–${filter.yearEnd}",
                                selected = true,
                                leadingIcon = LucideIcons.X,
                                onClick = { onSelectYearPreset(YearFilterPreset.ALL) }
                            )
                        }
                    }
                    if (filter.minRating != MinRatingFilter.ANY) {
                        item {
                            MinimalPill(
                                text = "★ ${filter.minRating.label}",
                                selected = true,
                                leadingIcon = LucideIcons.X,
                                onClick = { onSelectMinRating(MinRatingFilter.ANY) }
                            )
                        }
                    }
                    if (filter.durationFilter != DurationFilter.ANY) {
                        item {
                            MinimalPill(
                                text = filter.durationFilter.label,
                                selected = true,
                                leadingIcon = LucideIcons.X,
                                onClick = { onSelectDuration(DurationFilter.ANY) }
                            )
                        }
                    }
                    if (filter.hdOnly) {
                        item {
                            MinimalPill(
                                text = "HD",
                                selected = true,
                                leadingIcon = LucideIcons.X,
                                onClick = onToggleHdOnly
                            )
                        }
                    }
                    if (filter.offlineOnly) {
                        item {
                            MinimalPill(
                                text = "Офлайн",
                                selected = true,
                                leadingIcon = LucideIcons.X,
                                onClick = onToggleOfflineOnly
                            )
                        }
                    }
                    if (filter.favoritesOnly) {
                        item {
                            MinimalPill(
                                text = "Избранное",
                                selected = true,
                                leadingIcon = LucideIcons.X,
                                onClick = onToggleFavoritesOnly
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Найдено: ${uiState.filteredMovies.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textTertiary
                )
                if (filter.activeFilterCount > 0 || filter.query.isNotBlank()) {
                    Text(
                        text = "Сбросить",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.accent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onResetAll)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Animated Catalog Body Switching Smoothly Between Grid 2x / Compact Grid 3x / List
        if (uiState.filteredMovies.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                MinimalPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.Film,
                            contentDescription = null,
                            tint = palette.textTertiary,
                            modifier = Modifier.size(34.dp)
                        )
                        Text(
                            text = "Ничего не найдено",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "Измените параметры фильтрации или попробуйте поиск по онлайн-архиву.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MinimalSecondaryButton(text = "Сбросить", onClick = onResetAll)
                            if (filter.query.length >= 2 && !uiState.effectiveOfflineMode) {
                                MinimalPrimaryButton(
                                    text = if (uiState.isSyncingOnline) "Поиск..." else "Archive.org",
                                    onClick = onSearchOnlineArchive,
                                    enabled = !uiState.isSyncingOnline
                                )
                            }
                        }
                    }
                }
            }
        } else {
            AnimatedContent(
                targetState = prefs.cardLayoutMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier.weight(1f),
                label = "catalog_layout_anim"
            ) { layoutMode ->
                when (layoutMode) {
                    CardLayoutMode.LIST -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 18.dp,
                                end = 18.dp,
                                top = 4.dp,
                                bottom = 112.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(uiState.filteredMovies, key = { it.id }) { movie ->
                                MovieListRowCard(
                                    movie = movie,
                                    onClick = { onMovieClick(movie.id) },
                                    onToggleFavorite = { onToggleFavorite(movie.id) },
                                    showRating = prefs.showRatingOnCards,
                                    showMeta = prefs.showMetaOnCards,
                                    showGenres = prefs.showGenresOnCards
                                )
                            }
                        }
                    }

                    CardLayoutMode.GRID_2, CardLayoutMode.GRID_3 -> {
                        val columns = if (layoutMode == CardLayoutMode.GRID_3) {
                            GridCells.Fixed(3)
                        } else {
                            GridCells.Adaptive(minSize = 152.dp)
                        }
                        val spacing = if (layoutMode == CardLayoutMode.GRID_3) 10.dp else 14.dp
                        LazyVerticalGrid(
                            columns = columns,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 18.dp,
                                end = 18.dp,
                                top = 4.dp,
                                bottom = 112.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(spacing),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.filteredMovies, key = { it.id }) { movie ->
                                MoviePosterCard(
                                    movie = movie,
                                    onClick = { onMovieClick(movie.id) },
                                    onToggleFavorite = { onToggleFavorite(movie.id) },
                                    compact = layoutMode == CardLayoutMode.GRID_3,
                                    showRating = prefs.showRatingOnCards,
                                    showMeta = prefs.showMetaOnCards,
                                    showGenres = prefs.showGenresOnCards
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 1. Clean Filters Bottom Sheet
    if (showFiltersSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFiltersSheet = false },
            containerColor = palette.surface,
            contentColor = palette.textPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Фильтры", style = MaterialTheme.typography.titleLarge)
                    if (filter.activeFilterCount > 0) {
                        Text(
                            text = "Сбросить",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.accent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onResetFilters)
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }

                // Genres
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Жанры", style = MaterialTheme.typography.titleSmall, color = palette.textSecondary)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.availableGenres.forEach { genre ->
                            MinimalPill(
                                text = genre,
                                selected = genre in filter.selectedGenres,
                                onClick = { onToggleGenre(genre) }
                            )
                        }
                    }
                }

                HorizontalDivider(color = palette.borderSubtle)

                // Release Years
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Годы выпуска", style = MaterialTheme.typography.titleSmall, color = palette.textSecondary)
                        Text(
                            text = "${filter.yearStart} — ${filter.yearEnd}",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.textPrimary
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        YearFilterPreset.entries.filter { it != YearFilterPreset.CUSTOM }.forEach { preset ->
                            MinimalPill(
                                text = preset.label,
                                selected = filter.yearPreset == preset,
                                onClick = { onSelectYearPreset(preset) }
                            )
                        }
                    }
                    RangeSlider(
                        value = filter.yearStart.toFloat()..filter.yearEnd.toFloat(),
                        onValueChange = { range ->
                            onCustomYearRange(range.start.roundToInt(), range.endInclusive.roundToInt())
                        },
                        valueRange = 1900f..2026f,
                        colors = SliderDefaults.colors(
                            thumbColor = palette.ctaBackground,
                            activeTrackColor = palette.accent,
                            inactiveTrackColor = palette.border
                        )
                    )
                }

                HorizontalDivider(color = palette.borderSubtle)

                // Minimum Rating
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Минимальный рейтинг", style = MaterialTheme.typography.titleSmall, color = palette.textSecondary)
                    MinimalSegmentedBar(
                        options = MinRatingFilter.entries.map { it.label },
                        selectedIndex = MinRatingFilter.entries.indexOf(filter.minRating),
                        onSelect = { idx -> onSelectMinRating(MinRatingFilter.entries[idx]) }
                    )
                }

                // Duration
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Длительность", style = MaterialTheme.typography.titleSmall, color = palette.textSecondary)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DurationFilter.entries.forEach { dur ->
                            MinimalPill(
                                text = dur.label,
                                selected = filter.durationFilter == dur,
                                onClick = { onSelectDuration(dur) }
                            )
                        }
                    }
                }

                HorizontalDivider(color = palette.borderSubtle)

                // Quick Toggles
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    MinimalSwitchRow(
                        title = "Только в HD",
                        checked = filter.hdOnly,
                        onCheckedChange = { onToggleHdOnly() }
                    )
                    MinimalSwitchRow(
                        title = "Доступно офлайн",
                        checked = filter.offlineOnly,
                        onCheckedChange = { onToggleOfflineOnly() }
                    )
                    MinimalSwitchRow(
                        title = "Только из избранного",
                        checked = filter.favoritesOnly,
                        onCheckedChange = { onToggleFavoritesOnly() }
                    )
                }

                MinimalPrimaryButton(
                    text = "Показать (${uiState.filteredMovies.size})",
                    onClick = { showFiltersSheet = false },
                    useAccent = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // 2. Clean Sort Bottom Sheet
    if (showSortSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSortSheet = false },
            containerColor = palette.surface,
            contentColor = palette.textPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Сортировка", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(6.dp))
                SortOption.entries.forEach { option ->
                    val isSelected = filter.sortBy == option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) palette.surfaceElevated else palette.surface)
                            .clickable {
                                onSelectSort(option)
                                showSortSheet = false
                            }
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = palette.textPrimary
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = LucideIcons.Check,
                                contentDescription = null,
                                tint = palette.accent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // 3. Card Display Settings Bottom Sheet
    if (showViewSheet) {
        ModalBottomSheet(
            onDismissRequest = { showViewSheet = false },
            containerColor = palette.surface,
            contentColor = palette.textPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Отображение карточек", style = MaterialTheme.typography.titleLarge)

                MinimalSegmentedBar(
                    options = CardLayoutMode.entries.map { it.label },
                    selectedIndex = CardLayoutMode.entries.indexOf(prefs.cardLayoutMode),
                    onSelect = { idx -> onSelectCardLayout(CardLayoutMode.entries[idx]) }
                )

                HorizontalDivider(color = palette.borderSubtle)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    MinimalSwitchRow(
                        title = "Показывать рейтинг",
                        subtitle = "Оценка зрителей на постере",
                        checked = prefs.showRatingOnCards,
                        onCheckedChange = onSetShowRating
                    )
                    MinimalSwitchRow(
                        title = "Год и длительность",
                        subtitle = "Подпись под названием фильма",
                        checked = prefs.showMetaOnCards,
                        onCheckedChange = onSetShowMeta
                    )
                    MinimalSwitchRow(
                        title = "Теги жанров",
                        subtitle = "Основные жанры в карточке",
                        checked = prefs.showGenresOnCards,
                        onCheckedChange = onSetShowGenres
                    )
                }
            }
        }
    }
}
