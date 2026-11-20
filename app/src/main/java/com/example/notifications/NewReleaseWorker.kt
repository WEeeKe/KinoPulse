package com.example.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.KinoPulseDatabase
import com.example.data.local.NotificationItemEntity
import com.example.data.prefs.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.util.UUID

class NewReleaseWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val prefs = UserPreferencesRepository(applicationContext).preferencesFlow.first()
        if (!prefs.notificationsEnabled) return Result.success()

        val dao = KinoPulseDatabase.getInstance(applicationContext).kinoPulseDao()
        val newReleases = dao.getAllMoviesOnce().filter { it.isNewRelease }
        val featured = newReleases.randomOrNull() ?: return Result.success()

        val title = "Новая подборка: «${featured.title}»"
        val body = "${featured.genres.joinToString(", ")} · ${featured.releaseYear} · Рейтинг ${featured.rating}. Доступно для просмотра и скачивания в офлайн."

        dao.insertNotification(
            NotificationItemEntity(
                id = "notif_${UUID.randomUUID()}",
                title = title,
                body = body,
                movieId = featured.id,
                createdAt = System.currentTimeMillis(),
                isRead = false
            )
        )

        NewReleaseNotificationHelper.showNewReleaseNotification(
            context = applicationContext,
            notificationId = featured.id.hashCode(),
            title = title,
            body = body
        )

        return Result.success()
    }
}
