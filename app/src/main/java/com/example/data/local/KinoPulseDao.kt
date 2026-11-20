package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface KinoPulseDao {
    @Query("SELECT * FROM movies ORDER BY isFeatured DESC, rating DESC, releaseYear DESC")
    fun observeAllMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies")
    suspend fun getAllMoviesOnce(): List<MovieEntity>

    @Query("SELECT * FROM movies WHERE id = :movieId LIMIT 1")
    fun observeMovieById(movieId: String): Flow<MovieEntity?>

    @Query("SELECT * FROM movies WHERE id = :movieId LIMIT 1")
    suspend fun getMovieByIdOnce(movieId: String): MovieEntity?

    @Query("SELECT * FROM movies WHERE isFavorite = 1 ORDER BY lastWatchedAt DESC, rating DESC")
    fun observeFavorites(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isDownloaded = 1 ORDER BY cachedAt DESC")
    fun observeDownloadedMovies(): Flow<List<MovieEntity>>

    @Query("SELECT COUNT(*) FROM movies")
    suspend fun getMoviesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMovies(movies: List<MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMoviesIfAbsent(movies: List<MovieEntity>)

    @Query("UPDATE movies SET videoUrl = :videoUrl WHERE id = :movieId")
    suspend fun updateResolvedVideoUrl(movieId: String, videoUrl: String)

    @Query("UPDATE movies SET isFavorite = :isFavorite WHERE id = :movieId")
    suspend fun setFavorite(movieId: String, isFavorite: Boolean)

    @Query("UPDATE movies SET isFavorite = 1, userCollection = :collection WHERE id = :movieId")
    suspend fun setFavoriteCollection(movieId: String, collection: String)

    @Query("UPDATE movies SET watchProgressSeconds = :progressSeconds, lastWatchedAt = :watchedAt WHERE id = :movieId")
    suspend fun updateWatchProgress(movieId: String, progressSeconds: Long, watchedAt: Long)

    @Query("UPDATE movies SET isDownloaded = :downloaded, localVideoPath = :localPath, downloadSizeMb = :sizeMb, cachedAt = :cachedAt WHERE id = :movieId")
    suspend fun updateDownloadStatus(
        movieId: String,
        downloaded: Boolean,
        localPath: String?,
        sizeMb: Double,
        cachedAt: Long
    )

    // Chat
    @Query("SELECT * FROM chat_messages WHERE movieId = :movieId ORDER BY timestamp ASC")
    fun observeChatMessages(movieId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getChatCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessages(messages: List<ChatMessageEntity>)

    @Query("UPDATE chat_messages SET isLikedByMe = NOT isLikedByMe, likesCount = CASE WHEN isLikedByMe = 1 THEN MAX(0, likesCount - 1) ELSE likesCount + 1 END WHERE id = :messageId")
    suspend fun toggleChatMessageLike(messageId: String)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun observeNotifications(): Flow<List<NotificationItemEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun observeUnreadNotificationsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notifications")
    suspend fun getNotificationsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationItemEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markNotificationRead(notificationId: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsRead()
}
