package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: String,
    val title: String,
    val originalTitle: String,
    val tagline: String,
    val description: String,
    val genresCsv: String,
    val releaseYear: Int,
    val durationMinutes: Int,
    val rating: Double,
    val director: String,
    val castCsv: String,
    val posterKey: String,
    val posterUrl: String,
    val videoUrl: String,
    val archiveId: String = "",
    val country: String = "",
    val isHd: Boolean = true,
    val isNewRelease: Boolean = false,
    val isFeatured: Boolean = false,
    val isFavorite: Boolean = false,
    val userCollection: String = "Любимые",
    val watchProgressSeconds: Long = 0L,
    val lastWatchedAt: Long = 0L,
    val isDownloaded: Boolean = false,
    val localVideoPath: String? = null,
    val downloadSizeMb: Double = 0.0,
    val cachedAt: Long = System.currentTimeMillis()
) {
    val genres: List<String>
        get() = genresCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    val cast: List<String>
        get() = castCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val movieId: String,
    val senderName: String,
    val senderBadge: String,
    val isMine: Boolean,
    val text: String,
    val timestamp: Long,
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val replyToSender: String? = null,
    val replyToSnippet: String? = null
)

@Entity(tableName = "notifications")
data class NotificationItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val movieId: String?,
    val createdAt: Long,
    val isRead: Boolean = false
)
