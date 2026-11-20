package com.example.data.sync

import com.example.data.local.MovieEntity
import com.example.data.prefs.UserPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable
data class SyncMovieState(
    val movieId: String,
    val isFavorite: Boolean,
    val collection: String,
    val progressSeconds: Long,
    val lastWatchedAt: Long
)

@Serializable
data class SyncSnapshot(
    val version: Int = 1,
    val deviceId: String,
    val userName: String,
    val themeMode: String,
    val amoledNightMode: Boolean,
    val notificationsEnabled: Boolean,
    val exportedAt: Long,
    val movies: List<SyncMovieState>
)

object SyncManager {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun exportSnapshotCode(
        prefs: UserPreferences,
        movies: List<MovieEntity>
    ): String {
        val relevantMovies = movies.filter { it.isFavorite || it.watchProgressSeconds > 0L }
            .map {
                SyncMovieState(
                    movieId = it.id,
                    isFavorite = it.isFavorite,
                    collection = it.userCollection,
                    progressSeconds = it.watchProgressSeconds,
                    lastWatchedAt = it.lastWatchedAt
                )
            }
        val snapshot = SyncSnapshot(
            deviceId = prefs.deviceId,
            userName = prefs.userDisplayName,
            themeMode = prefs.themeMode.name,
            amoledNightMode = prefs.amoledNightMode,
            notificationsEnabled = prefs.notificationsEnabled,
            exportedAt = System.currentTimeMillis(),
            movies = relevantMovies
        )
        val rawJson = json.encodeToString(snapshot)
        return "KP1:" + Base64.encode(rawJson.toByteArray(Charsets.UTF_8))
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun parseSnapshotCode(code: String): Result<SyncSnapshot> {
        return runCatching {
            val trimmed = code.trim()
            val decoded = if (trimmed.startsWith("KP1:")) {
                val b64 = trimmed.removePrefix("KP1:")
                String(Base64.decode(b64), Charsets.UTF_8)
            } else {
                trimmed
            }
            json.decodeFromString<SyncSnapshot>(decoded)
        }
    }
}
