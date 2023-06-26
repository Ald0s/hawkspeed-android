package com.vljx.hawkspeed.domain.repository

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.domain.requestmodels.user.RequestGetUser
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    /**
     * Open a flow for a User from the cache, and at the same time query the latest User record from the server.
     */
    fun getUserByUid(requestGetUser: RequestGetUser): Flow<Resource<User>>
}