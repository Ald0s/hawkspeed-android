package com.vljx.hawkspeed.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.vljx.hawkspeed.data.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UserDao: BaseDao<UserEntity>() {
    @Query("""
        SELECT *
        FROM user
        WHERE userUid = :userUid
    """)
    abstract fun selectUserByUid(userUid: String): Flow<UserEntity?>

    @Query("""
        DELETE FROM user
        WHERE userUid = :userUid
    """)
    abstract suspend fun deleteUserByUid(userUid: String)

    @Query("""
        DELETE FROM user
    """)
    abstract suspend fun clearAllUsers()
}