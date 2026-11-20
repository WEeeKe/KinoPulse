package com.example.data.network

import com.example.data.local.MovieEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

/**
 * Live zero-key integration with Internet Archive's open cinema library (archive.org).
 * Fetches real public-domain feature films, posters, metadata, and resolves direct MP4 streams.
 */
class ArchiveOrgMovieService(
    private val client: OkHttpClient
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchPopularFeatureFilms(page: Int = 1, rows: Int = 18): Result<List<MovieEntity>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "https://archive.org/advancedsearch.php".toHttpUrl().newBuilder()
                    .addQueryParameter("q", "collection:(feature_films) AND mediatype:(movies) AND format:(MPEG4)")
                    .addQueryParameter("fl[]", "identifier")
                    .addQueryParameter("fl[]", "title")
                    .addQueryParameter("fl[]", "year")
                    .addQueryParameter("fl[]", "description")
                    .addQueryParameter("fl[]", "creator")
                    .addQueryParameter("fl[]", "avg_rating")
                    .addQueryParameter("fl[]", "subject")
                    .addQueryParameter("fl[]", "runtime")
                    .addQueryParameter("sort[]", "downloads desc")
                    .addQueryParameter("rows", rows.toString())
                    .addQueryParameter("page", page.toString())
                    .addQueryParameter("output", "json")
                    .build()

                executeSearch(url.toString())
            }
        }

    suspend fun searchArchiveMovies(query: String, rows: Int = 16): Result<List<MovieEntity>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val sanitized = query.trim().replace("\"", "")
                if (sanitized.isBlank()) return@runCatching emptyList()
                val q = "collection:(feature_films) AND mediatype:(movies) AND format:(MPEG4) AND (title:($sanitized) OR description:($sanitized) OR creator:($sanitized))"
                val url = "https://archive.org/advancedsearch.php".toHttpUrl().newBuilder()
                    .addQueryParameter("q", q)
                    .addQueryParameter("fl[]", "identifier")
                    .addQueryParameter("fl[]", "title")
                    .addQueryParameter("fl[]", "year")
                    .addQueryParameter("fl[]", "description")
                    .addQueryParameter("fl[]", "creator")
                    .addQueryParameter("fl[]", "avg_rating")
                    .addQueryParameter("fl[]", "subject")
                    .addQueryParameter("fl[]", "runtime")
                    .addQueryParameter("sort[]", "downloads desc")
                    .addQueryParameter("rows", rows.toString())
                    .addQueryParameter("output", "json")
                    .build()

                executeSearch(url.toString())
            }
        }

    suspend fun resolveDirectMp4Url(archiveId: String): String? = withContext(Dispatchers.IO) {
        if (archiveId.isBlank()) return@withContext null
        runCatching {
            val request = Request.Builder()
                .url("https://archive.org/metadata/$archiveId")
                .header("User-Agent", "KinoPulse-Android/2.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@runCatching null
                val body = response.body?.string() ?: return@runCatching null
                val root = json.parseToJsonElement(body).jsonObject
                val files = root["files"]?.jsonArray ?: return@runCatching null

                val mp4Candidates = files.mapNotNull { elem ->
                    val obj = elem as? JsonObject ?: return@mapNotNull null
                    val name = obj["name"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                    if (!name.endsWith(".mp4", ignoreCase = true)) return@mapNotNull null
                    if (name.contains("sample", ignoreCase = true)) return@mapNotNull null
                    val format = obj["format"]?.jsonPrimitive?.contentOrNull.orEmpty()
                    val sizeBytes = obj["size"]?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: 0L
                    val lengthSec = obj["length"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() ?: 0.0
                    val priority = when {
                        format.contains("512Kb", ignoreCase = true) -> 2
                        format.contains("h.264", ignoreCase = true) -> 4
                        format.contains("MPEG4", ignoreCase = true) -> 3
                        else -> 1
                    }
                    Mp4Candidate(name, priority, lengthSec, sizeBytes)
                }

                // Prefer real full-length H.264/MPEG4 stream (reasonable mobile bitrate if multiple exist)
                val best = mp4Candidates
                    .sortedWith(
                        compareByDescending<Mp4Candidate> { it.lengthSec > 60.0 }
                            .thenByDescending { it.priority }
                            .thenByDescending { it.sizeBytes in 5_000_000L..1_800_000_000L }
                            .thenByDescending { it.sizeBytes }
                    )
                    .firstOrNull() ?: return@runCatching null

                val encodedName = best.name.split("/").joinToString("/") { segment ->
                    URLEncoder.encode(segment, "UTF-8").replace("+", "%20")
                }
                "https://archive.org/download/$archiveId/$encodedName"
            }
        }.getOrNull()
    }

    private fun executeSearch(url: String): List<MovieEntity> {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "KinoPulse-Android/2.0")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Archive.org HTTP ${response.code}")
            }
            val raw = response.body?.string() ?: return emptyList()
            val root = json.parseToJsonElement(raw).jsonObject
            val docs = root["response"]?.jsonObject?.get("docs")?.jsonArray ?: return emptyList()

            return docs.mapNotNull { doc ->
                val obj = doc as? JsonObject ?: return@mapNotNull null
                val identifier = extractString(obj["identifier"])?.trim().orEmpty()
                val title = extractString(obj["title"])?.trim().orEmpty()
                if (identifier.isBlank() || title.isBlank()) return@mapNotNull null

                val year = extractYear(obj["year"])
                val description = extractString(obj["description"])
                    ?.replace(Regex("<[^>]*>"), " ")
                    ?.replace(Regex("\\s+"), " ")
                    ?.trim()
                    ?.take(480)
                    ?.ifBlank { "Полнометражный фильм из открытой коллекции мирового кино Internet Archive." }
                    ?: "Полнометражный фильм из открытой коллекции мирового кино Internet Archive."

                val creator = extractString(obj["creator"])?.trim()
                    ?.ifBlank { "Классика кино" } ?: "Классика кино"

                val rawRating = extractDouble(obj["avg_rating"])
                // Archive.org avg_rating is 0..5 -> convert to familiar 0..10 IMDb-like scale
                val normalizedRating = if (rawRating != null && rawRating > 0.0) {
                    (((rawRating * 1.6) + 1.4).coerceIn(6.2, 9.2) * 10).toInt() / 10.0
                } else {
                    7.4
                }

                val genres = mapSubjectsToRussianGenres(extractList(obj["subject"]))
                val runtimeMinutes = parseRuntimeMinutes(extractString(obj["runtime"]))
                val fallbackKeys = listOf("horizon", "neon_rain", "alpine_echo", "hero_cinema")
                val fallbackKey = fallbackKeys[Math.abs(identifier.hashCode()) % fallbackKeys.size]

                MovieEntity(
                    id = "ia_$identifier",
                    title = title,
                    originalTitle = title,
                    tagline = "Мировое кино · Internet Archive",
                    description = description,
                    genresCsv = genres.joinToString(","),
                    releaseYear = year,
                    durationMinutes = runtimeMinutes,
                    rating = normalizedRating,
                    director = creator,
                    castCsv = "Архивная коллекция",
                    posterKey = fallbackKey,
                    posterUrl = "https://archive.org/services/img/$identifier",
                    videoUrl = "",
                    archiveId = identifier,
                    country = "Международный",
                    isHd = year >= 2000,
                    isNewRelease = year >= 2015,
                    isFeatured = false
                )
            }
        }
    }

    private fun extractString(element: JsonElement?): String? = when (element) {
        is JsonPrimitive -> element.contentOrNull
        is JsonArray -> element.firstOrNull()?.let { (it as? JsonPrimitive)?.contentOrNull }
        else -> null
    }

    private fun extractList(element: JsonElement?): List<String> = when (element) {
        is JsonPrimitive -> element.contentOrNull?.split(";", ",")?.map { it.trim() } ?: emptyList()
        is JsonArray -> element.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
        else -> emptyList()
    }

    private fun extractDouble(element: JsonElement?): Double? = when (element) {
        is JsonPrimitive -> element.doubleOrNull ?: element.contentOrNull?.toDoubleOrNull()
        is JsonArray -> element.firstOrNull()?.let { extractDouble(it) }
        else -> null
    }

    private fun extractYear(element: JsonElement?): Int {
        val raw = extractString(element)
        val parsed = (element as? JsonPrimitive)?.intOrNull
            ?: raw?.let { Regex("(19|20)\\d{2}").find(it)?.value?.toIntOrNull() }
        return (parsed ?: 1955).coerceIn(1900, 2026)
    }

    private fun parseRuntimeMinutes(raw: String?): Int {
        if (raw.isNullOrBlank()) return 85
        val parts = raw.split(":").mapNotNull { it.trim().toIntOrNull() }
        return when (parts.size) {
            3 -> (parts[0] * 60 + parts[1]).coerceIn(5, 240)
            2 -> parts[0].coerceIn(5, 240)
            else -> Regex("\\d+").find(raw)?.value?.toIntOrNull()?.coerceIn(5, 240) ?: 85
        }
    }

    private fun mapSubjectsToRussianGenres(subjects: List<String>): List<String> {
        val joined = subjects.joinToString(" ").lowercase()
        val result = linkedSetOf<String>()
        if (joined.contains("sci-fi") || joined.contains("science fiction")) result.add("Фантастика")
        if (joined.contains("horror") || joined.contains("zombie") || joined.contains("vampire")) result.add("Ужасы")
        if (joined.contains("noir")) result.add("Нуар")
        if (joined.contains("thriller") || joined.contains("suspense")) result.add("Триллер")
        if (joined.contains("mystery") || joined.contains("detective") || joined.contains("crime")) result.add("Детектив")
        if (joined.contains("comedy") || joined.contains("slapstick")) result.add("Комедия")
        if (joined.contains("drama") || joined.contains("romance")) result.add("Драма")
        if (joined.contains("action") || joined.contains("western") || joined.contains("war")) result.add("Боевик")
        if (joined.contains("adventure")) result.add("Приключения")
        if (joined.contains("animation") || joined.contains("cartoon")) result.add("Анимация")
        if (joined.contains("fantasy")) result.add("Фэнтези")
        if (result.isEmpty()) result.add("Драма")
        return result.take(3)
    }

    private data class Mp4Candidate(
        val name: String,
        val priority: Int,
        val lengthSec: Double,
        val sizeBytes: Long
    )
}
