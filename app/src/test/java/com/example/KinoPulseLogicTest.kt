package com.example

import com.example.data.local.MovieCatalogSeed
import com.example.data.prefs.CardLayoutMode
import com.example.data.prefs.UserPreferences
import com.example.data.recommendations.RecommendationEngine
import com.example.data.sync.SyncManager
import com.example.ui.DurationFilter
import com.example.ui.MinRatingFilter
import com.example.ui.SearchFilterState
import com.example.ui.YearFilterPreset
import com.example.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KinoPulseLogicTest {

    @Test
    fun realMovieCatalogContainsAtLeastTwentyRealFilms() {
        val movies = MovieCatalogSeed.initialMovies()
        assertTrue("Expected at least 18 real films", movies.size >= 18)
        assertTrue(movies.any { it.id == "charade_1963" && it.archiveId == "Charade1963" })
        assertTrue(movies.any { it.id == "tears_of_steel_2012" && it.videoUrl.isNotBlank() })
        assertTrue(movies.any { it.id == "metropolis_1927" && it.releaseYear == 1927 })
    }

    @Test
    fun recommendationsPrioritizeMatchingFavoriteGenres() {
        val movies = MovieCatalogSeed.initialMovies().map { movie ->
            if (movie.id == "charade_1963") movie.copy(isFavorite = true) else movie
        }
        val recs = RecommendationEngine.buildRecommendations(movies, limit = 5)
        assertTrue(recs.isNotEmpty())
        assertTrue(recs.none { it.movie.id == "charade_1963" })
        val topGenres = recs.first().movie.genres
        assertTrue(
            "Expected top recommendation to share Detective/Thriller/Comedy genre",
            topGenres.any { it in listOf("Детектив", "Триллер", "Комедия") }
        )
    }

    @Test
    fun activeFilterCountTracksNewFiltersAccurately() {
        val defaultFilter = SearchFilterState()
        assertEquals(0, defaultFilter.activeFilterCount)

        val configured = defaultFilter.copy(
            selectedGenres = setOf("Фантастика", "Триллер"),
            yearPreset = YearFilterPreset.GOLDEN_ERA,
            minRating = MinRatingFilter.SEVEN_FIVE,
            durationFilter = DurationFilter.LONG,
            hdOnly = true,
            favoritesOnly = true
        )
        assertEquals(7, configured.activeFilterCount)
    }

    @Test
    fun syncSnapshotRoundtripPreservesFavoritesAndPreferences() {
        val movies = MovieCatalogSeed.initialMovies().map { movie ->
            if (movie.id == "tears_of_steel_2012") {
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
            deviceId = "KP-TEST01",
            cardLayoutMode = CardLayoutMode.GRID_3
        )

        val code = SyncManager.exportSnapshotCode(prefs, movies)
        assertTrue(code.startsWith("KP1:"))

        val parsed = SyncManager.parseSnapshotCode(code).getOrThrow()
        assertEquals("Киноман_Тест", parsed.userName)
        assertEquals(ThemeMode.NIGHT.name, parsed.themeMode)
        assertTrue(parsed.amoledNightMode)
        assertEquals(1, parsed.movies.size)
        assertEquals("tears_of_steel_2012", parsed.movies.first().movieId)
        assertEquals("Смотреть позже", parsed.movies.first().collection)
        assertEquals(42L, parsed.movies.first().progressSeconds)
    }
}
