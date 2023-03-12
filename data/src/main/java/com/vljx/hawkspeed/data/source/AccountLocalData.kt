package com.vljx.hawkspeed.data.source

import com.vljx.hawkspeed.data.models.account.AccountModel
import kotlinx.coroutines.flow.Flow

interface AccountLocalData {
    fun selectAccountByUid(userUid: String): Flow<AccountModel?>

    suspend fun setAccountLoggedIn(accountModel: AccountModel)
    suspend fun accountLoggedOut(accountModel: AccountModel)
    suspend fun upsertAccount(accountModel: AccountModel)
}