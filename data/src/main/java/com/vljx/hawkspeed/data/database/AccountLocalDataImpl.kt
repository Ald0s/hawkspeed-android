package com.vljx.hawkspeed.data.database

import com.vljx.hawkspeed.data.database.dao.AccountDao
import com.vljx.hawkspeed.data.database.entity.AccountEntity
import com.vljx.hawkspeed.data.database.mapper.AccountEntityMapper
import com.vljx.hawkspeed.data.models.account.AccountModel
import com.vljx.hawkspeed.data.source.account.AccountLocalData
import com.vljx.hawkspeed.domain.exc.AccountChangedException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class AccountLocalDataImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val appDatabase: AppDatabase,

    private val accountEntityMapper: AccountEntityMapper
): AccountLocalData {
    override fun selectCurrentAccount(): Flow<AccountModel?> =
        accountDao.selectCurrentAccount().map { accountEntity ->
            accountEntity?.run { accountEntityMapper.mapFromEntity(accountEntity) }
        }

    override suspend fun upsertAccount(account: AccountModel) {
        // We'll first get the current account that's in cache, if any.
        val accountEntity: AccountEntity? = accountDao.getCurrentAccount()
        // If the account is not null, and the uids do not match the one we will upsert, the current User has not properly logged out.
        if(accountEntity != null && accountEntity.userUid != account.userUid) {
            // Throw an account changed exception, which will signal to repository that authentication status has been lost.
            throw AccountChangedException()
        }
        // Upsert the given account into cache.
        accountDao.upsert(
            accountEntityMapper.mapToEntity(account)
        )
    }

    override suspend fun clearAccount() {
        CoroutineScope(Dispatchers.IO).launch {
            appDatabase.clearAllTables()
        }
    }
}