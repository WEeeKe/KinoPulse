package com.example.data.cache

import android.content.Context
import coil.imageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class CacheStats(
    val offlineVideosCount: Int = 0,
    val offlineVideosSizeMb: Double = 0.0,
    val imageCacheSizeMb: Double = 0.0,
    val httpCacheSizeMb: Double = 0.0
) {
    val totalCacheMb: Double
        get() = offlineVideosSizeMb + imageCacheSizeMb + httpCacheSizeMb
}

sealed interface DownloadResult {
    data class Success(val localPath: String, val sizeMb: Double) : DownloadResult
    data class Error(val message: String) : DownloadResult
}

class OfflineCacheManager(private val context: Context) {
    private val offlineMoviesDir = File(context.filesDir, "offline_movies").apply { mkdirs() }
    private val httpCacheDir = File(context.cacheDir, "okhttp_cache").apply { mkdirs() }

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cache(Cache(httpCacheDir, 50L * 1024L * 1024L))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .build()
    }

    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Float>> = _downloadProgress.asStateFlow()

    suspend fun downloadMovieForOffline(movieId: String, videoUrl: String): DownloadResult =
        withContext(Dispatchers.IO) {
            val targetFile = File(offlineMoviesDir, "$movieId.mp4")
            val tempFile = File(offlineMoviesDir, "$movieId.tmp")
            try {
                _downloadProgress.update { it + (movieId to 0.02f) }
                val request = Request.Builder().url(videoUrl).build()
                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext DownloadResult.Error(
                            "Сервер вернул код ${response.code}. Попробуйте позже."
                        )
                    }
                    val body = response.body
                        ?: return@withContext DownloadResult.Error("Пустой поток видео.")
                    val totalBytes = body.contentLength()
                    var downloadedBytes = 0L

                    body.byteStream().use { input ->
                        FileOutputStream(tempFile).use { output ->
                            val buffer = ByteArray(16 * 1024)
                            while (true) {
                                val read = input.read(buffer)
                                if (read == -1) break
                                output.write(buffer, 0, read)
                                downloadedBytes += read
                                if (totalBytes > 0) {
                                    val progress = (downloadedBytes.toFloat() / totalBytes.toFloat())
                                        .coerceIn(0.02f, 0.99f)
                                    _downloadProgress.update { it + (movieId to progress) }
                                }
                            }
                        }
                    }

                    if (targetFile.exists()) targetFile.delete()
                    tempFile.renameTo(targetFile)
                    val sizeMb = targetFile.length().toDouble() / (1024.0 * 1024.0)
                    DownloadResult.Success(targetFile.absolutePath, sizeMb)
                }
            } catch (e: Exception) {
                tempFile.delete()
                DownloadResult.Error(
                    "Не удалось загрузить фильм: проверьте подключение к интернету."
                )
            } finally {
                _downloadProgress.update { it - movieId }
            }
        }

    suspend fun deleteOfflineMovie(movieId: String, localPath: String?) = withContext(Dispatchers.IO) {
        if (!localPath.isNullOrBlank()) {
            runCatching { File(localPath).delete() }
        }
        runCatching { File(offlineMoviesDir, "$movieId.mp4").delete() }
    }

    @OptIn(coil.annotation.ExperimentalCoilApi::class)
    suspend fun clearVolatileCaches() = withContext(Dispatchers.IO) {
        runCatching {
            context.imageLoader.memoryCache?.clear()
            context.imageLoader.diskCache?.clear()
        }
        runCatching {
            okHttpClient.cache?.evictAll()
        }
    }

    @OptIn(coil.annotation.ExperimentalCoilApi::class)
    suspend fun getCacheStats(): CacheStats = withContext(Dispatchers.IO) {
        val videoFiles = offlineMoviesDir.listFiles()?.filter { it.isFile && it.extension == "mp4" }
            ?: emptyList()
        val videoBytes = videoFiles.sumOf { it.length() }
        val imageBytes = context.imageLoader.diskCache?.size ?: folderSize(File(context.cacheDir, "image_cache"))
        val httpBytes = okHttpClient.cache?.size() ?: folderSize(httpCacheDir)

        CacheStats(
            offlineVideosCount = videoFiles.size,
            offlineVideosSizeMb = videoBytes / (1024.0 * 1024.0),
            imageCacheSizeMb = imageBytes / (1024.0 * 1024.0),
            httpCacheSizeMb = httpBytes / (1024.0 * 1024.0)
        )
    }

    private fun folderSize(dir: File): Long {
        if (!dir.exists()) return 0L
        return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }
}
