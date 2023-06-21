package com.vljx.hawkspeed.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.vljx.hawkspeed.data.database.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AccountDao: BaseDao<AccountEntity>() {
    @Query("""
        SELECT *
        FROM account
        LIMIT 1
    """)
    abstract fun selectCurrentAccount(): Flow<AccountEntity?>

    @Query("""
        SELECT *
        FROM account
        LIMIT 1
    """)
    abstract suspend fun getCurrentAccount(): AccountEntity?

    @Query("""
        DELETE FROM account
    """)
    abstract suspend fun clearCurrentAccount()
}