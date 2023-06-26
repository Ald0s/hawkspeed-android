package com.vljx.hawkspeed.data.database

import com.vljx.hawkspeed.data.database.dao.UserDao
import com.vljx.hawkspeed.data.database.entity.UserEntity
import com.vljx.hawkspeed.data.database.mapper.UserEntityMapper
import com.vljx.hawkspeed.data.models.user.UserModel
import com.vljx.hawkspeed.data.source.user.UserLocalData
import com.vljx.hawkspeed.domain.requestmodels.user.RequestGetUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserLocalDataImpl @Inject constructor(
    private val userDao: UserDao,

    private val userEntityMapper: UserEntityMapper
): UserLocalData {
    override fun selectUserByUid(requestGetUser: RequestGetUser): Flow<UserModel?> =
        userDao.selectUserByUid(requestGetUser.userUid)
            .map { value: UserEntity? ->
                value?.let { userEntityMapper.mapFromEntity(it) }
            }

    override suspend fun upsertUser(user: UserModel) =
        userDao.upsert(
            userEntityMapper.mapToEntity(user)
        )

    override suspend fun upsertUsers(users: List<UserModel>) =
        userDao.upsert(
            userEntityMapper.mapToEntityList(users)
        )

    override suspend fun deleteUser(userUid: String) =
        userDao.deleteUserByUid(userUid)

    override suspend fun clearAllUsers() =
        userDao.clearAllUsers()
}