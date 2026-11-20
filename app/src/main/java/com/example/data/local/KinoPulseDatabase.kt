package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MovieEntity::class,
        ChatMessageEntity::class,
        NotificationItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KinoPulseDatabase : RoomDatabase() {
    abstract fun kinoPulseDao(): KinoPulseDao

    companion object {
        @Volatile
        private var INSTANCE: KinoPulseDatabase? = null

        fun getInstance(context: Context): KinoPulseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KinoPulseDatabase::class.java,
                    "kinopulse_cache.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    seedIfEmpty(database.kinoPulseDao())
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedIfEmpty(dao: KinoPulseDao) {
            if (dao.getMoviesCount() == 0) {
                dao.upsertMovies(MovieCatalogSeed.initialMovies())
            }
            if (dao.getChatCount() == 0) {
                dao.insertChatMessages(MovieCatalogSeed.initialChatMessages())
            }
            if (dao.getNotificationsCount() == 0) {
                dao.insertNotifications(MovieCatalogSeed.initialNotifications())
            }
        }
    }
}
