package com.vljx.hawkspeed.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vljx.hawkspeed.data.database.dao.AccountDao
import com.vljx.hawkspeed.data.database.dao.TrackDao
import com.vljx.hawkspeed.data.database.dao.TrackPointDao
import com.vljx.hawkspeed.data.database.entity.AccountEntity
import com.vljx.hawkspeed.data.database.entity.TrackEntity
import com.vljx.hawkspeed.data.database.entity.TrackPointEntity
import com.vljx.hawkspeed.data.database.entity.UserEntity

@Database(entities = [AccountEntity::class, UserEntity::class, TrackEntity::class, TrackPointEntity::class],
    version = 4
)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun trackDao(): TrackDao
    abstract fun trackPointDao(): TrackPointDao

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