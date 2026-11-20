package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.cache.CacheStats
import com.example.data.cache.DownloadResult
import com.example.data.local.ChatMessageEntity
import com.example.data.local.MovieEntity
import com.example.data.local.NotificationItemEntity
import com.example.data.prefs.UserPreferences
import com.example.data.recommendations.RecommendationEngine
import com.example.data.recommendations.RecommendedMovie
import com.example.data.repository.KinoPulseRepository
import com.example.notifications.NewReleaseNotificationHelper
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class YearFilterPreset(val label: String) {
    ALL("Все годы"),
    RECENT_2024_2026("2024–2026"),
    ERA_2020_2023("2020–2023"),
    ERA_2010_2019("2010–2019"),
    CLASSIC_PRE_2010("До 2010"),
    CUSTOM("Диапазон")
}

enum class SortOption(val label: String) {
    RATING("По рейтингу"),
    YEAR_NEW("Сначала новые"),
    DURATION("Короткие")
}

data class SearchFilterState(
    val query: String = "",
    val selectedGenres: Set<String> = emptySet(),
    val yearPreset: YearFilterPreset = YearFilterPreset.ALL,
    val yearStart: Int = 1994,
    val yearEnd: Int = 2026,
    val sortBy: SortOption = SortOption.RATING,
    val offlineOnly: Boolean = false
)

data class MainUiState(
    val allMovies: List<MovieEntity> = emptyList(),
    val featuredMovie: MovieEntity? = null,
    val newReleases: List<MovieEntity> = emptyList(),
    val recommendations: List<RecommendedMovie> = emptyList(),
    val continueWatching: List<MovieEntity> = emptyList(),
    val favorites: List<MovieEntity> = emptyList(),
    val downloadedMovies: List<MovieEntity> = emptyList(),
    val notifications: List<NotificationItemEntity> = emptyList(),
    val unreadNotificationsCount: Int = 0,
    val preferences: UserPreferences = UserPreferences(),
    val isDeviceOnline: Boolean = true,
    val downloadProgress: Map<String, Float> = emptyMap(),
    val cacheStats: CacheStats = CacheStats(),
    val searchFilter: SearchFilterState = SearchFilterState(),
    val filteredMovies: List<MovieEntity> = emptyList(),
    val availableGenres: List<String> = emptyList(),
    val snackbarMessage: String? = null
) {
    val effectiveOfflineMode: Boolean
        get() = preferences.offlineModeEnabled || !isDeviceOnline
}

class KinoPulseViewModel(
    private val repository: KinoPulseRepository,
    private val appContext: android.content.Context
) : ViewModel() {

    private val _searchFilter = MutableStateFlow(SearchFilterState())
    private val _cacheStats = MutableStateFlow(CacheStats())
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    private val moviesBundleFlow = combine(
        repository.allMovies,
        repository.favorites,
        repository.downloadedMovies,
        repository.notifications,
        repository.unreadNotificationsCount
    ) { all, favs, downloaded, notifs, unread ->
        MoviesBundle(all, favs, downloaded, notifs, unread)
    }

    private val systemBundleFlow = combine(
        repository.preferences,
        repository.isDeviceOnline,
        repository.downloadProgress,
        _cacheStats,
        _searchFilter
    ) { prefs, online, progress, cache, filter ->
        SystemBundle(prefs, online, progress, cache, filter)
    }

    val uiState: StateFlow<MainUiState> = combine(
        moviesBundleFlow,
        systemBundleFlow,
        _snackbarMessage
    ) { movies, system, snackbar ->
        val all = movies.all
        val featured = all.firstOrNull { it.isFeatured } ?: all.firstOrNull()
        val newReleases = all.filter { it.isNewRelease }
        val recommendations = RecommendationEngine.buildRecommendations(all, limit = 8)
        val continueWatching = all.filter { it.watchProgressSeconds > 0L }
            .sortedByDescending { it.lastWatchedAt }
        val genres = all.flatMap { it.genres }.distinct().sorted()
        val filtered = applySearchFilter(all, system.filter)

        MainUiState(
            allMovies = all,
            featuredMovie = featured,
            newReleases = newReleases,
            recommendations = recommendations,
            continueWatching = continueWatching,
            favorites = movies.favorites,
            downloadedMovies = movies.downloaded,
            notifications = movies.notifications,
            unreadNotificationsCount = movies.unreadCount,
            preferences = system.prefs,
            isDeviceOnline = system.online,
            downloadProgress = system.progress,
            cacheStats = system.cache,
            searchFilter = system.filter,
            filteredMovies = filtered,
            availableGenres = genres,
            snackbarMessage = snackbar
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = MainUiState()
    )

    init {
        refreshCacheStats()
    }

    fun observeMovie(movieId: String): Flow<MovieEntity?> = repository.observeMovie(movieId)

    fun observeChat(movieId: String): Flow<List<ChatMessageEntity>> = repository.observeChat(movieId)

    fun relatedMovies(movie: MovieEntity, allMovies: List<MovieEntity>): List<RecommendedMovie> =
        RecommendationEngine.relatedMovies(movie, allMovies)

    fun toggleFavorite(movieId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(movieId)
        }
    }

    fun setFavoriteCollection(movieId: String, collection: String) {
        viewModelScope.launch {
            repository.setCollection(movieId, collection)
            _snackbarMessage.value = "Добавлено в коллекцию «$collection»"
        }
    }

    fun updateWatchProgress(movieId: String, progressSeconds: Long) {
        viewModelScope.launch {
            repository.updateWatchProgress(movieId, progressSeconds)
        }
    }

    fun downloadMovie(movie: MovieEntity) {
        viewModelScope.launch {
            val result = repository.downloadMovie(movie)
            refreshCacheStats()
            _snackbarMessage.value = when (result) {
                is DownloadResult.Success ->
                    "«${movie.title}» сохранён для офлайн (${String.format("%.1f", result.sizeMb)} МБ)"
                is DownloadResult.Error -> result.message
            }
        }
    }

    fun removeDownload(movie: MovieEntity) {
        viewModelScope.launch {
            repository.removeDownloadedMovie(movie)
            refreshCacheStats()
            _snackbarMessage.value = "Офлайн-копия «${movie.title}» удалена"
        }
    }

    fun refreshCacheStats() {
        viewModelScope.launch {
            _cacheStats.value = repository.getCacheStats()
        }
    }

    fun clearVolatileCache() {
        viewModelScope.launch {
            _cacheStats.value = repository.clearVolatileCache()
            _snackbarMessage.value = "Временный кеш изображений и сети очищен"
        }
    }

    fun toggleNightMode() {
        viewModelScope.launch {
            repository.prefsRepo.toggleNightMode()
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.prefsRepo.setThemeMode(mode)
        }
    }

    fun setAmoledNightMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.prefsRepo.setAmoledNightMode(enabled)
        }
    }

    fun setOfflineMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.prefsRepo.setOfflineMode(enabled)
            _snackbarMessage.value = if (enabled) {
                "Офлайн-режим включён: показываем сохранённый каталог и загрузки"
            } else {
                "Офлайн-режим отключён"
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.prefsRepo.setNotificationsEnabled(enabled)
            NewReleaseNotificationHelper.schedulePeriodicChecks(appContext, enabled)
        }
    }

    fun triggerNewReleaseNotification() {
        viewModelScope.launch {
            repository.triggerNewReleaseAlert()
            _snackbarMessage.value = "Уведомление о новинке отправлено"
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
        }
    }

    fun setUserDisplayName(name: String) {
        viewModelScope.launch {
            repository.prefsRepo.setUserDisplayName(name)
            _snackbarMessage.value = "Имя профиля обновлено"
        }
    }

    fun sendChatMessage(movieId: String, text: String, replyTo: ChatMessageEntity? = null) {
        viewModelScope.launch {
            repository.sendChatMessage(movieId, text, replyTo)
        }
    }

    fun toggleChatLike(messageId: String) {
        viewModelScope.launch {
            repository.toggleMessageLike(messageId)
        }
    }

    fun exportSyncCode(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val code = repository.exportSyncCode()
            onReady(code)
        }
    }

    fun importSyncCode(code: String) {
        viewModelScope.launch {
            val result = repository.importSyncCode(code)
            result.onSuccess { count ->
                _snackbarMessage.value = "Синхронизация завершена: обновлено фильмов ($count)"
            }.onFailure {
                _snackbarMessage.value = "Некорректный код синхронизации. Проверьте формат."
            }
        }
    }

    // Search & Filters
    fun updateSearchQuery(query: String) {
        _searchFilter.update { it.copy(query = query) }
    }

    fun toggleGenreFilter(genre: String) {
        _searchFilter.update { state ->
            val next = if (genre in state.selectedGenres) {
                state.selectedGenres - genre
            } else {
                state.selectedGenres + genre
            }
            state.copy(selectedGenres = next)
        }
    }

    fun selectSingleGenreAndReset(genre: String) {
        _searchFilter.update {
            SearchFilterState(selectedGenres = setOf(genre))
        }
    }

    fun selectYearPreset(preset: YearFilterPreset) {
        val (start, end) = when (preset) {
            YearFilterPreset.ALL -> 1994 to 2026
            YearFilterPreset.RECENT_2024_2026 -> 2024 to 2026
            YearFilterPreset.ERA_2020_2023 -> 2020 to 2023
            YearFilterPreset.ERA_2010_2019 -> 2010 to 2019
            YearFilterPreset.CLASSIC_PRE_2010 -> 1994 to 2009
            YearFilterPreset.CUSTOM -> _searchFilter.value.yearStart to _searchFilter.value.yearEnd
        }
        _searchFilter.update {
            it.copy(yearPreset = preset, yearStart = start, yearEnd = end)
        }
    }

    fun updateCustomYearRange(start: Int, end: Int) {
        _searchFilter.update {
            it.copy(
                yearPreset = YearFilterPreset.CUSTOM,
                yearStart = minOf(start, end),
                yearEnd = maxOf(start, end)
            )
        }
    }

    fun updateSortOption(option: SortOption) {
        _searchFilter.update { it.copy(sortBy = option) }
    }

    fun toggleOfflineOnlyFilter() {
        _searchFilter.update { it.copy(offlineOnly = !it.offlineOnly) }
    }

    fun resetSearchFilters() {
        _searchFilter.value = SearchFilterState()
    }

    fun consumeSnackbar() {
        _snackbarMessage.value = null
    }

    private fun applySearchFilter(
        movies: List<MovieEntity>,
        filter: SearchFilterState
    ): List<MovieEntity> {
        val q = filter.query.trim().lowercase()
        val matched = movies.filter { movie ->
            val matchesQuery = q.isEmpty() ||
                movie.title.lowercase().contains(q) ||
                movie.originalTitle.lowercase().contains(q) ||
                movie.director.lowercase().contains(q) ||
                movie.genresCsv.lowercase().contains(q) ||
                movie.releaseYear.toString().contains(q)

            val matchesGenre = filter.selectedGenres.isEmpty() ||
                filter.selectedGenres.any { selected -> selected in movie.genres }

            val matchesYear = movie.releaseYear in filter.yearStart..filter.yearEnd

            val matchesOffline = !filter.offlineOnly || movie.isDownloaded

            matchesQuery && matchesGenre && matchesYear && matchesOffline
        }

        return when (filter.sortBy) {
            SortOption.RATING -> matched.sortedByDescending { it.rating }
            SortOption.YEAR_NEW -> matched.sortedByDescending { it.releaseYear }
            SortOption.DURATION -> matched.sortedBy { it.durationMinutes }
        }
    }

    private data class MoviesBundle(
        val all: List<MovieEntity>,
        val favorites: List<MovieEntity>,
        val downloaded: List<MovieEntity>,
        val notifications: List<NotificationItemEntity>,
        val unreadCount: Int
    )

    private data class SystemBundle(
        val prefs: UserPreferences,
        val online: Boolean,
        val progress: Map<String, Float>,
        val cache: CacheStats,
        val filter: SearchFilterState
    )

    class Factory(
        private val repository: KinoPulseRepository,
        private val appContext: android.content.Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return KinoPulseViewModel(repository, appContext) as T
        }
    }
}
