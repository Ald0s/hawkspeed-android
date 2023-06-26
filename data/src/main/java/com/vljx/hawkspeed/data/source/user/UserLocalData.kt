package com.vljx.hawkspeed.data.source.user

import com.vljx.hawkspeed.data.models.user.UserModel
import com.vljx.hawkspeed.domain.requestmodels.user.RequestGetUser
import kotlinx.coroutines.flow.Flow

interface UserLocalData {
    /**
     * Open a flow for the desired User from the cache.
     */
    fun selectUserByUid(requestGetUser: RequestGetUser): Flow<UserModel?>

    /**
     * Upsert the given User into cache.
     */
    suspend fun upsertUser(user: UserModel)

    /**
     * Upsert the list of User into cache.
     */
    suspend fun upsertUsers(users: List<UserModel>)

    /**
     * Delete the desired User from cache.
     */
    suspend fun deleteUser(userUid: String)

    /**
     * Clear all Users from cache.
     */
    suspend fun clearAllUsers()
}