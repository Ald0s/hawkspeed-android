package com.vljx.hawkspeed.data

import com.vljx.hawkspeed.data.mapper.user.UserMapper
import com.vljx.hawkspeed.data.source.UserRemoteData
import com.vljx.hawkspeed.data.source.user.UserLocalData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.domain.repository.UserRepository
import com.vljx.hawkspeed.domain.requestmodels.user.RequestGetUser
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userRemoteData: UserRemoteData,
    private val userLocalData: UserLocalData,

    private val userMapper: UserMapper
): BaseRepository(), UserRepository {
    override fun getUserByUid(requestGetUser: RequestGetUser): Flow<Resource<User>> =
        flowQueryFromCacheNetworkAndCache(
            userMapper,
            databaseQuery = { userLocalData.selectUserByUid(requestGetUser) },
            networkQuery = { userRemoteData.queryUserByUid(requestGetUser) },
            cacheResult = { user ->
                userLocalData.upsertUser(user)
            }
        )
}