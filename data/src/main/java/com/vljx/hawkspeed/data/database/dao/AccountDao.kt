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
        WHERE userUid = :userUid
    """)
    abstract fun selectAccountByUid(userUid: String): Flow<AccountEntity?>

    @Query("""
        UPDATE account
        SET isInUse = 0
        WHERE userUid <> :exceptUserUid
    """)
    abstract suspend fun setAllUnused(exceptUserUid: String)
}