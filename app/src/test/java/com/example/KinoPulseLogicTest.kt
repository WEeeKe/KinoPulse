package com.example

import com.example.data.local.MovieCatalogSeed
import com.example.data.prefs.UserPreferences
import com.example.data.recommendations.RecommendationEngine
import com.example.data.sync.SyncManager
import com.example.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KinoPulseLogicTest {

    @Test
    fun recommendationsPrioritizeMatchingFavoriteGenres() {
        val movies = MovieCatalogSeed.initialMovies().map { movie ->
            if (movie.id == "neon_rain") movie.copy(isFavorite = true) else movie
        }
        val recs = RecommendationEngine.buildRecommendations(movies, limit = 5)
        assertTrue(recs.isNotEmpty())
        assertTrue(recs.none { it.movie.id == "neon_rain" })
        val topGenres = recs.first().movie.genres
        assertTrue(
            "Expected top recommendation to share Thriller/Detective/Sci-Fi genre",
            topGenres.any { it in listOf("Триллер", "Детектив", "Фантастика") }
        )
    }

    @Test
    fun syncSnapshotRoundtripPreservesFavoritesAndPreferences() {
        val movies = MovieCatalogSeed.initialMovies().map { movie ->
            if (movie.id == "horizon_event") {
                movie.copy(isFavorite = true, userCollection = "Смотреть позже", watchProgressSeconds = 42L)
            } else {
                movie
            }
        }
        val prefs = UserPreferences(
            themeMode = ThemeMode.NIGHT,
            amoledNightMode = true,
            offlineModeEnabled = false,
            notificationsEnabled = true,
            userDisplayName = "Киноман_Тест",
            deviceId = "KP-TEST01"
        )

        val code = SyncManager.exportSnapshotCode(prefs, movies)
        assertTrue(code.startsWith("KP1:"))

        val parsed = SyncManager.parseSnapshotCode(code).getOrThrow()
        assertEquals("Киноман_Тест", parsed.userName)
        assertEquals(ThemeMode.NIGHT.name, parsed.themeMode)
        assertTrue(parsed.amoledNightMode)
        assertEquals(1, parsed.movies.size)
        assertEquals("horizon_event", parsed.movies.first().movieId)
        assertEquals("Смотреть позже", parsed.movies.first().collection)
        assertEquals(42L, parsed.movies.first().progressSeconds)
    }
}
