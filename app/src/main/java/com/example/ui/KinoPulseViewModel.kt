package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.cache.CacheStats
import com.example.data.cache.DownloadResult
import com.example.data.local.ChatMessageEntity
import com.example.data.local.MovieEntity
import com.example.data.local.NotificationItemEntity
import com.example.data.prefs.CardLayoutMode
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
    MODERN("2000–2026"),
    MID_CENTURY("1950–1999"),
    GOLDEN_ERA("До 1950"),
    CUSTOM("Диапазон")
}

enum class MinRatingFilter(val label: String, val minRating: Double) {
    ANY("Любой", 0.0),
    SEVEN("7.0+", 7.0),
    SEVEN_FIVE("7.5+", 7.5),
    EIGHT("8.0+", 8.0)
}

enum class DurationFilter(val label: String) {
    ANY("Любая"),
    SHORT("До 60 мин"),
    MEDIUM("60–100 мин"),
    LONG("100+ мин")
}

enum class SortOption(val label: String) {
    RATING("По рейтингу"),
    YEAR_NEW("Сначала новые"),
    YEAR_OLD("Сначала классика"),
    DURATION("Короткие"),
    TITLE("По названию")
}

data class SearchFilterState(
    val query: String = "",
    val selectedGenres: Set<String> = emptySet(),
    val yearPreset: YearFilterPreset = YearFilterPreset.ALL,
    val yearStart: Int = 1900,
    val yearEnd: Int = 2026,
    val minRating: MinRatingFilter = MinRatingFilter.ANY,
    val durationFilter: DurationFilter = DurationFilter.ANY,
    val sortBy: SortOption = SortOption.RATING,
    val offlineOnly: Boolean = false,
    val favoritesOnly: Boolean = false,
    val hdOnly: Boolean = false
) {
    val activeFilterCount: Int
        get() {
            var count = selectedGenres.size
            if (yearPreset != YearFilterPreset.ALL) count++
            if (minRating != MinRatingFilter.ANY) count++
            if (durationFilter != DurationFilter.ANY) count++
            if (offlineOnly) count++
            if (favoritesOnly) count++
            if (hdOnly) count++
            return count
        }
}

data class MainUiState(
    val allMovies: List<MovieEntity> = emptyList(),
    val featuredMovies: List<MovieEntity> = emptyList(),
    val hdSelections: List<MovieEntity> = emptyList(),
    val classicCinema: List<MovieEntity> = emptyList(),
    val recommendations: List<RecommendedMovie> = emptyList(),
    val continueWatching: List<MovieEntity> = emptyList(),
    val favorites: List<MovieEntity> = emptyList(),
    val downloadedMovies: List<MovieEntity> = emptyList(),
    val notifications: List<NotificationItemEntity> = emptyList(),
    val unreadNotificationsCount: Int = 0,
    val preferences: UserPreferences = UserPreferences(),
    val isDeviceOnline: Boolean = true,
    val isSyncingOnline: Boolean = false,
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
    private val _isSyncingOnline = MutableStateFlow(false)
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
        _isSyncingOnline,
        _snackbarMessage
    ) { movies, system, syncing, snackbar ->
        val all = movies.all
        val featured = all.filter { it.isFeatured }.ifEmpty { all.take(3) }
        val hdSelections = all.filter { it.isHd }.sortedByDescending { it.releaseYear }
        val classicCinema = all.filter { it.releaseYear < 1980 }.sortedByDescending { it.rating }
        val recommendations = RecommendationEngine.buildRecommendations(all, limit = 8)
        val continueWatching = all.filter { it.watchProgressSeconds > 0L }
            .sortedByDescending { it.lastWatchedAt }
        val genres = all.flatMap { it.genres }.distinct().sorted()
        val filtered = applySearchFilter(all, system.filter)

        MainUiState(
            allMovies = all,
            featuredMovies = featured,
            hdSelections = hdSelections,
            classicCinema = classicCinema,
            recommendations = recommendations,
            continueWatching = continueWatching,
            favorites = movies.favorites,
            downloadedMovies = movies.downloaded,
            notifications = movies.notifications,
            unreadNotificationsCount = movies.unreadCount,
            preferences = system.prefs,
            isDeviceOnline = system.online,
            isSyncingOnline = syncing,
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

    suspend fun resolvePlayableVideoUrl(movie: MovieEntity): String? =
        repository.resolvePlayableVideoUrl(movie)

    fun syncMoreRealMovies(nextPage: Boolean = true) {
        if (_isSyncingOnline.value) return
        viewModelScope.launch {
            _isSyncingOnline.value = true
            val result = repository.syncOnlineCatalog(nextPage = nextPage)
            _isSyncingOnline.value = false
            result.onSuccess { added ->
                _snackbarMessage.value = if (added > 0) {
                    "Загружено $added новых фильмов из Internet Archive"
                } else {
                    "Каталог актуален — новые фильмы уже в базе"
                }
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "Не удалось загрузить каталог из сети"
            }
        }
    }

    fun searchOnlineArchive() {
        val query = _searchFilter.value.query.trim()
        if (query.length < 2 || _isSyncingOnline.value) return
        viewModelScope.launch {
            _isSyncingOnline.value = true
            val result = repository.searchArchiveOnline(query)
            _isSyncingOnline.value = false
            result.onSuccess { added ->
                _snackbarMessage.value = if (added > 0) {
                    "Найдено и добавлено из Archive.org: $added"
                } else {
                    "Новых совпадений в Archive.org не найдено"
                }
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "Ошибка онлайн-поиска"
            }
        }
    }

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
            _snackbarMessage.value = "Временный кеш очищен"
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

    fun setCardLayoutMode(mode: CardLayoutMode) {
        viewModelScope.launch {
            repository.prefsRepo.setCardLayoutMode(mode)
        }
    }

    fun cycleCardLayoutMode() {
        viewModelScope.launch {
            repository.prefsRepo.cycleCardLayoutMode()
        }
    }

    fun setShowRatingOnCards(show: Boolean) {
        viewModelScope.launch {
            repository.prefsRepo.setShowRatingOnCards(show)
        }
    }

    fun setShowMetaOnCards(show: Boolean) {
        viewModelScope.launch {
            repository.prefsRepo.setShowMetaOnCards(show)
        }
    }

    fun setShowGenresOnCards(show: Boolean) {
        viewModelScope.launch {
            repository.prefsRepo.setShowGenresOnCards(show)
        }
    }

    fun setOfflineMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.prefsRepo.setOfflineMode(enabled)
            _snackbarMessage.value = if (enabled) {
                "Офлайн-режим включён"
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
            _snackbarMessage.value = "Уведомление отправлено"
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
                _snackbarMessage.value = "Синхронизация завершена: обновлено ($count)"
            }.onFailure {
                _snackbarMessage.value = "Некорректный код синхронизации"
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
            YearFilterPreset.ALL -> 1900 to 2026
            YearFilterPreset.MODERN -> 2000 to 2026
            YearFilterPreset.MID_CENTURY -> 1950 to 1999
            YearFilterPreset.GOLDEN_ERA -> 1900 to 1949
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

    fun selectMinRating(filter: MinRatingFilter) {
        _searchFilter.update { it.copy(minRating = filter) }
    }

    fun selectDurationFilter(filter: DurationFilter) {
        _searchFilter.update { it.copy(durationFilter = filter) }
    }

    fun updateSortOption(option: SortOption) {
        _searchFilter.update { it.copy(sortBy = option) }
    }

    fun toggleOfflineOnlyFilter() {
        _searchFilter.update { it.copy(offlineOnly = !it.offlineOnly) }
    }

    fun toggleFavoritesOnlyFilter() {
        _searchFilter.update { it.copy(favoritesOnly = !it.favoritesOnly) }
    }

    fun toggleHdOnlyFilter() {
        _searchFilter.update { it.copy(hdOnly = !it.hdOnly) }
    }

    fun resetSearchFilters() {
        _searchFilter.update { SearchFilterState(query = it.query) }
    }

    fun resetAllSearchAndQuery() {
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
                movie.castCsv.lowercase().contains(q) ||
                movie.genresCsv.lowercase().contains(q) ||
                movie.releaseYear.toString().contains(q)

            val matchesGenre = filter.selectedGenres.isEmpty() ||
                filter.selectedGenres.any { selected -> selected in movie.genres }

            val matchesYear = movie.releaseYear in filter.yearStart..filter.yearEnd
            val matchesRating = movie.rating >= filter.minRating.minRating
            val matchesDuration = when (filter.durationFilter) {
                DurationFilter.ANY -> true
                DurationFilter.SHORT -> movie.durationMinutes < 60
                DurationFilter.MEDIUM -> movie.durationMinutes in 60..100
                DurationFilter.LONG -> movie.durationMinutes > 100
            }
            val matchesOffline = !filter.offlineOnly || movie.isDownloaded
            val matchesFav = !filter.favoritesOnly || movie.isFavorite
            val matchesHd = !filter.hdOnly || movie.isHd

            matchesQuery && matchesGenre && matchesYear && matchesRating &&
                matchesDuration && matchesOffline && matchesFav && matchesHd
        }

        return when (filter.sortBy) {
            SortOption.RATING -> matched.sortedByDescending { it.rating }
            SortOption.YEAR_NEW -> matched.sortedByDescending { it.releaseYear }
            SortOption.YEAR_OLD -> matched.sortedBy { it.releaseYear }
            SortOption.DURATION -> matched.sortedBy { it.durationMinutes }
            SortOption.TITLE -> matched.sortedBy { it.title.lowercase() }
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
