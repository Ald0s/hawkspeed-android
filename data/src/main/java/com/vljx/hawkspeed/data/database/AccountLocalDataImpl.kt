package com.vljx.hawkspeed.data.database

import com.vljx.hawkspeed.data.database.dao.AccountDao
import com.vljx.hawkspeed.data.database.entity.AccountEntity
import com.vljx.hawkspeed.data.database.mapper.AccountEntityMapper
import com.vljx.hawkspeed.data.models.account.AccountModel
import com.vljx.hawkspeed.data.source.AccountLocalData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AccountLocalDataImpl @Inject constructor(
    private val accountDao: AccountDao,

    private val accountEntityMapper: AccountEntityMapper
): AccountLocalData {
    override fun selectAccountByUid(userUid: String): Flow<AccountModel?> {
        val accountEntityFlow: Flow<AccountEntity?> = accountDao.selectAccountByUid(userUid)
        return accountEntityFlow.map { accountEntity ->
            accountEntity?.run { accountEntityMapper.mapFromEntity(accountEntity) }
        }
    }

    override suspend fun setAccountLoggedIn(accountModel: AccountModel) {
        // Convert model to an entity.
        val accountEntity: AccountEntity = accountEntityMapper.mapToEntity(accountModel)
        // isUnUse will always be false after mapping. So here we'll set it to true.
        accountEntity.isInUse = true
        // Now we will set all other account entities to not be in use, where the account not the same as this one.
        // We specify this WHERE clause to avoid a potential update being emitted that could log the account out...
        accountDao.setAllUnused(accountEntity.userUid)
        // Now upsert our entity.
        accountDao.upsert(accountEntity)
    }

    override suspend fun accountLoggedOut(accountModel: AccountModel) {
        // Map to entity.
        val accountEntity: AccountEntity = accountEntityMapper.mapToEntity(accountModel)
        // We'll now set isInUse to false manually.
        accountEntity.isInUse = false
        // And upsert.
        accountDao.upsert(accountEntity)
    }

    override suspend fun upsertAccount(accountModel: AccountModel) {
        // Map to entity.
        val accountEntity: AccountEntity = accountEntityMapper.mapToEntity(accountModel)
        // Upsert the account.
        accountDao.upsert(accountEntity)
    }
}