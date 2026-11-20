package com.example.data.repository

import android.content.Context
import com.example.data.cache.CacheStats
import com.example.data.cache.DownloadResult
import com.example.data.cache.OfflineCacheManager
import com.example.data.local.ChatMessageEntity
import com.example.data.local.KinoPulseDao
import com.example.data.local.KinoPulseDatabase
import com.example.data.local.MovieEntity
import com.example.data.local.NotificationItemEntity
import com.example.data.network.ArchiveOrgMovieService
import com.example.data.network.NetworkMonitor
import com.example.data.prefs.UserPreferences
import com.example.data.prefs.UserPreferencesRepository
import com.example.data.sync.SyncManager
import com.example.notifications.NewReleaseNotificationHelper
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import java.util.UUID

class KinoPulseRepository(
    private val context: Context,
    private val dao: KinoPulseDao,
    val prefsRepo: UserPreferencesRepository,
    val cacheManager: OfflineCacheManager,
    val networkMonitor: NetworkMonitor,
    private val archiveService: ArchiveOrgMovieService
) {
    val allMovies: Flow<List<MovieEntity>> = dao.observeAllMovies()
    val favorites: Flow<List<MovieEntity>> = dao.observeFavorites()
    val downloadedMovies: Flow<List<MovieEntity>> = dao.observeDownloadedMovies()
    val notifications: Flow<List<NotificationItemEntity>> = dao.observeNotifications()
    val unreadNotificationsCount: Flow<Int> = dao.observeUnreadNotificationsCount()
    val preferences: Flow<UserPreferences> = prefsRepo.preferencesFlow
    val isDeviceOnline: StateFlow<Boolean> = networkMonitor.isOnline
    val downloadProgress: StateFlow<Map<String, Float>> = cacheManager.downloadProgress

    private var archivePage = 1

    suspend fun initialize() {
        KinoPulseDatabase.seedIfEmpty(dao)
        prefsRepo.ensureDeviceId()
        NewReleaseNotificationHelper.ensureChannel(context)
        val prefs = prefsRepo.preferencesFlow.first()
        NewReleaseNotificationHelper.schedulePeriodicChecks(context, prefs.notificationsEnabled)
    }

    suspend fun syncOnlineCatalog(nextPage: Boolean = false): Result<Int> {
        val prefs = prefsRepo.preferencesFlow.first()
        if (prefs.offlineModeEnabled || !isDeviceOnline.value) {
            return Result.failure(IllegalStateException("Офлайн-режим: подключение к сети недоступно"))
        }
        val pageToFetch = if (nextPage) archivePage + 1 else 1
        val result = archiveService.fetchPopularFeatureFilms(page = pageToFetch, rows = 16)
        return result.map { fetched ->
            if (fetched.isNotEmpty()) {
                val beforeCount = dao.getMoviesCount()
                dao.insertMoviesIfAbsent(fetched)
                val afterCount = dao.getMoviesCount()
                if (nextPage) archivePage = pageToFetch
                (afterCount - beforeCount).coerceAtLeast(0)
            } else {
                0
            }
        }
    }

    suspend fun searchArchiveOnline(query: String): Result<Int> {
        val prefs = prefsRepo.preferencesFlow.first()
        if (prefs.offlineModeEnabled || !isDeviceOnline.value) {
            return Result.failure(IllegalStateException("Офлайн-режим: онлайн-поиск недоступен"))
        }
        val result = archiveService.searchArchiveMovies(query = query, rows = 14)
        return result.map { fetched ->
            if (fetched.isNotEmpty()) {
                val before = dao.getMoviesCount()
                dao.insertMoviesIfAbsent(fetched)
                (dao.getMoviesCount() - before).coerceAtLeast(0)
            } else {
                0
            }
        }
    }

    suspend fun resolvePlayableVideoUrl(movie: MovieEntity): String? {
        if (movie.videoUrl.isNotBlank()) return movie.videoUrl
        if (movie.archiveId.isBlank()) return null
        val resolved = archiveService.resolveDirectMp4Url(movie.archiveId)
        if (!resolved.isNullOrBlank()) {
            dao.updateResolvedVideoUrl(movie.id, resolved)
        }
        return resolved
    }

    fun observeMovie(movieId: String): Flow<MovieEntity?> = dao.observeMovieById(movieId)

    fun observeChat(movieId: String): Flow<List<ChatMessageEntity>> = dao.observeChatMessages(movieId)

    suspend fun toggleFavorite(movieId: String) {
        val movie = dao.getMovieByIdOnce(movieId) ?: return
        dao.setFavorite(movieId, !movie.isFavorite)
    }

    suspend fun setCollection(movieId: String, collection: String) {
        dao.setFavoriteCollection(movieId, collection)
    }

    suspend fun updateWatchProgress(movieId: String, progressSeconds: Long) {
        dao.updateWatchProgress(movieId, progressSeconds, System.currentTimeMillis())
    }

    suspend fun downloadMovie(movie: MovieEntity): DownloadResult {
        val resolvedUrl = resolvePlayableVideoUrl(movie)
            ?: return DownloadResult.Error("Не удалось получить ссылку на MP4-поток. Проверьте сеть.")
        val result = cacheManager.downloadMovieForOffline(movie.id, resolvedUrl)
        if (result is DownloadResult.Success) {
            dao.updateDownloadStatus(
                movieId = movie.id,
                downloaded = true,
                localPath = result.localPath,
                sizeMb = result.sizeMb,
                cachedAt = System.currentTimeMillis()
            )
        }
        return result
    }

    suspend fun removeDownloadedMovie(movie: MovieEntity) {
        cacheManager.deleteOfflineMovie(movie.id, movie.localVideoPath)
        dao.updateDownloadStatus(
            movieId = movie.id,
            downloaded = false,
            localPath = null,
            sizeMb = 0.0,
            cachedAt = System.currentTimeMillis()
        )
    }

    suspend fun getCacheStats(): CacheStats = cacheManager.getCacheStats()

    suspend fun clearVolatileCache(): CacheStats {
        cacheManager.clearVolatileCaches()
        return cacheManager.getCacheStats()
    }

    suspend fun sendChatMessage(
        movieId: String,
        text: String,
        replyTo: ChatMessageEntity? = null
    ) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val prefs = prefsRepo.preferencesFlow.first()
        val message = ChatMessageEntity(
            id = "msg_${UUID.randomUUID()}",
            movieId = movieId,
            senderName = prefs.userDisplayName,
            senderBadge = "Вы",
            isMine = true,
            text = trimmed,
            timestamp = System.currentTimeMillis(),
            likesCount = 0,
            isLikedByMe = false,
            replyToSender = replyTo?.senderName,
            replyToSnippet = replyTo?.text?.take(60)
        )
        dao.insertChatMessage(message)
    }

    suspend fun toggleMessageLike(messageId: String) {
        dao.toggleChatMessageLike(messageId)
    }

    suspend fun triggerNewReleaseAlert(movie: MovieEntity? = null) {
        val target = movie ?: dao.getAllMoviesOnce().filter { it.isFeatured || it.isNewRelease }.randomOrNull()
            ?: dao.getAllMoviesOnce().firstOrNull() ?: return
        val title = "Рекомендация KinoPulse: «${target.title}»"
        val body = "${target.releaseYear} · ${target.genres.joinToString(", ")} · ★ ${target.rating}. Доступно для просмотра и загрузки в офлайн."
        val notif = NotificationItemEntity(
            id = "notif_${UUID.randomUUID()}",
            title = title,
            body = body,
            movieId = target.id,
            createdAt = System.currentTimeMillis(),
            isRead = false
        )
        dao.insertNotification(notif)
        NewReleaseNotificationHelper.showNewReleaseNotification(
            context = context,
            notificationId = target.id.hashCode(),
            title = title,
            body = body
        )
    }

    suspend fun markNotificationRead(id: String) = dao.markNotificationRead(id)

    suspend fun markAllNotificationsRead() = dao.markAllNotificationsRead()

    suspend fun exportSyncCode(): String {
        val prefs = prefsRepo.preferencesFlow.first()
        val movies = dao.getAllMoviesOnce()
        prefsRepo.updateLastSyncTimestamp()
        return SyncManager.exportSnapshotCode(prefs, movies)
    }

    suspend fun importSyncCode(code: String): Result<Int> {
        val parsed = SyncManager.parseSnapshotCode(code)
        return parsed.map { snapshot ->
            var updatedCount = 0
            runCatching { ThemeMode.valueOf(snapshot.themeMode) }.getOrNull()?.let {
                prefsRepo.setThemeMode(it)
            }
            prefsRepo.setAmoledNightMode(snapshot.amoledNightMode)
            prefsRepo.setNotificationsEnabled(snapshot.notificationsEnabled)
            if (snapshot.userName.isNotBlank()) {
                prefsRepo.setUserDisplayName(snapshot.userName)
            }
            snapshot.movies.forEach { state ->
                val existing = dao.getMovieByIdOnce(state.movieId)
                if (existing != null) {
                    if (state.isFavorite) {
                        dao.setFavoriteCollection(state.movieId, state.collection)
                    }
                    if (state.progressSeconds > existing.watchProgressSeconds) {
                        dao.updateWatchProgress(state.movieId, state.progressSeconds, state.lastWatchedAt)
                    }
                    updatedCount++
                }
            }
            prefsRepo.updateLastSyncTimestamp()
            updatedCount
        }
    }
}
