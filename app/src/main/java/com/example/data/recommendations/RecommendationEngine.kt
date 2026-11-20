package com.example.data.recommendations

import com.example.data.local.MovieEntity
import kotlin.math.abs
import kotlin.math.roundToInt

data class RecommendedMovie(
    val movie: MovieEntity,
    val matchPercent: Int,
    val reason: String
)

object RecommendationEngine {
    fun buildRecommendations(
        allMovies: List<MovieEntity>,
        limit: Int = 8
    ): List<RecommendedMovie> {
        if (allMovies.isEmpty()) return emptyList()

        val favorites = allMovies.filter { it.isFavorite }
        val watched = allMovies.filter { it.watchProgressSeconds > 0L }
        val signalMovies = (favorites + watched).distinctBy { it.id }

        if (signalMovies.isEmpty()) {
            return allMovies
                .sortedWith(compareByDescending<MovieEntity> { it.isNewRelease }.thenByDescending { it.rating })
                .take(limit)
                .map { movie ->
                    val score = (80 + ((movie.rating - 7.5) * 8).roundToInt()).coerceIn(82, 98)
                    val reason = if (movie.isNewRelease) {
                        "Громкая новинка ${movie.releaseYear} года · Рейтинг ${movie.rating}"
                    } else {
                        "Выбор редакции KinoPulse · ${movie.genres.firstOrNull() ?: "Кино"}"
                    }
                    RecommendedMovie(movie, score, reason)
                }
        }

        val genreScores = mutableMapOf<String, Double>()
        signalMovies.forEach { movie ->
            val weight = (if (movie.isFavorite) 2.0 else 1.0) +
                (if (movie.watchProgressSeconds > 15L) 0.5 else 0.0)
            movie.genres.forEach { genre ->
                genreScores[genre] = (genreScores[genre] ?: 0.0) + weight
            }
        }

        val avgPreferredYear = signalMovies.map { it.releaseYear }.average()
        val favoriteIds = favorites.map { it.id }.toSet()

        val candidates = allMovies.filter { it.id !in favoriteIds }
            .ifEmpty { allMovies }

        return candidates.map { candidate ->
            val matchedGenres = candidate.genres.filter { genreScores.containsKey(it) }
            val genreAffinity = matchedGenres.sumOf { genreScores[it] ?: 0.0 }
            val maxPossibleAffinity = genreScores.values.sortedDescending().take(2).sum().coerceAtLeast(1.0)
            val normalizedGenre = (genreAffinity / maxPossibleAffinity).coerceIn(0.0, 1.0)

            val yearDistance = abs(candidate.releaseYear - avgPreferredYear)
            val eraScore = (1.0 - (yearDistance / 25.0)).coerceIn(0.2, 1.0)
            val ratingScore = ((candidate.rating - 7.0) / 2.5).coerceIn(0.3, 1.0)
            val noveltyBonus = if (candidate.isNewRelease) 0.08 else 0.0

            val rawScore = (normalizedGenre * 0.55) + (ratingScore * 0.25) + (eraScore * 0.20) + noveltyBonus
            val matchPercent = (72 + (rawScore * 27).roundToInt()).coerceIn(74, 99)

            val anchorTitle = signalMovies.firstOrNull { sig ->
                sig.genres.any { it in candidate.genres }
            }?.title

            val reason = when {
                matchedGenres.isNotEmpty() && anchorTitle != null ->
                    "Похоже на «$anchorTitle» · Жанр: ${matchedGenres.take(2).joinToString(", ")}"
                matchedGenres.isNotEmpty() ->
                    "Подходит под ваши вкусы: ${matchedGenres.joinToString(", ")}"
                candidate.isNewRelease ->
                    "Свежая премьера ${candidate.releaseYear} года с рейтингом ${candidate.rating}"
                else ->
                    "Высокий зрительский рейтинг ${candidate.rating}"
            }

            RecommendedMovie(candidate, matchPercent, reason)
        }
            .sortedByDescending { it.matchPercent }
            .take(limit)
    }

    fun relatedMovies(target: MovieEntity, allMovies: List<MovieEntity>, limit: Int = 5): List<RecommendedMovie> {
        return allMovies
            .filter { it.id != target.id }
            .map { other ->
                val overlap = other.genres.count { it in target.genres }
                val yearDiff = abs(other.releaseYear - target.releaseYear)
                val score = (overlap * 25 + (10 - (yearDiff / 3).coerceAtMost(10)) + (other.rating * 4)).roundToInt()
                val match = (70 + score.coerceAtMost(28)).coerceIn(72, 98)
                val common = other.genres.filter { it in target.genres }.joinToString(", ")
                val reason = if (common.isNotBlank()) "Общие жанры: $common" else "Похожая атмосфера и рейтинг ${other.rating}"
                RecommendedMovie(other, match, reason)
            }
            .sortedByDescending { it.matchPercent }
            .take(limit)
    }
}
