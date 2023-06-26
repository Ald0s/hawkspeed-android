package com.vljx.hawkspeed.data.source.account

import com.vljx.hawkspeed.data.models.account.AccountModel
import kotlinx.coroutines.flow.Flow

interface AccountLocalData {
    /**
     * Select the current account saved in the cache. This can return null if there is none.
     */
    fun selectCurrentAccount(): Flow<AccountModel?>

    /**
     * Attempt to upsert the given account into cache. If there is already an account cached, whose UID does not match the incoming account's UID,
     * this function will throw an AccountChangedException to indicate that the User must re-login.
     */
    suspend fun upsertAccount(account: AccountModel)

    /**
     * Clear the entire room cache. This should only be called on logouts, or User change.
     */
    suspend fun clearAccount()
}