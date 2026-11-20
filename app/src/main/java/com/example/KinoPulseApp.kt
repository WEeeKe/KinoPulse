package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.data.cache.OfflineCacheManager
import com.example.data.local.KinoPulseDatabase
import com.example.data.network.NetworkMonitor
import com.example.data.prefs.UserPreferencesRepository
import com.example.data.repository.KinoPulseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class KinoPulseApp : Application(), ImageLoaderFactory {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var repository: KinoPulseRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = KinoPulseDatabase.getInstance(this)
        val prefsRepo = UserPreferencesRepository(this)
        val cacheManager = OfflineCacheManager(this)
        val networkMonitor = NetworkMonitor(this)

        repository = KinoPulseRepository(
            context = this,
            dao = database.kinoPulseDao(),
            prefsRepo = prefsRepo,
            cacheManager = cacheManager,
            networkMonitor = networkMonitor
        )

        appScope.launch {
            repository.initialize()
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(150L * 1024L * 1024L)
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
    }
}
