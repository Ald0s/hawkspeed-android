package com.vljx.hawkspeed.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vljx.hawkspeed.data.database.dao.*
import com.vljx.hawkspeed.data.database.entity.*
import com.vljx.hawkspeed.data.database.entity.track.TrackCommentEntity
import com.vljx.hawkspeed.data.database.entity.track.TrackEntity
import com.vljx.hawkspeed.data.database.entity.track.TrackPathEntity
import com.vljx.hawkspeed.data.database.entity.track.TrackPointEntity

@Database(
    entities = [AccountEntity::class, UserEntity::class, TrackEntity::class, TrackPathEntity::class, TrackPointEntity::class, RaceEntity::class, RaceOutcomeEntity::class, TrackCommentEntity::class],
    version = 41
)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun trackDao(): TrackDao
    abstract fun trackPathDao(): TrackPathDao
    abstract fun trackPointDao(): TrackPointDao
    abstract fun raceDao(): RaceDao
    abstract fun raceOutComeDao(): RaceOutcomeDao
    abstract fun userDao(): UserDao
    abstract fun trackCommentDao(): TrackCommentDao

    companion object {
        const val DATABASE_NAME = "hawkdb"

        @Volatile
        private var instance: AppDatabase? = null
        private val lock = Any()

        operator fun invoke(context: Context, memoryDatabase: Boolean = false) = instance
            ?: synchronized(lock) {
                instance ?: buildDatabase(context, memoryDatabase).also {
                    instance = it
                }
            }

        // TODO: a better way of determining whether we should supply a memory database.
        private fun buildDatabase(context: Context, memoryDatabase: Boolean) = when(memoryDatabase) {
            true -> Room.inMemoryDatabaseBuilder(
                context,
                AppDatabase::class.java
            )
                .build()

            else -> Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addCallback(object: RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}