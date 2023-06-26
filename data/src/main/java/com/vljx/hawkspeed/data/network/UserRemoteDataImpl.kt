package com.vljx.hawkspeed.data.network

import com.vljx.hawkspeed.data.models.user.UserModel
import com.vljx.hawkspeed.data.network.api.UserService
import com.vljx.hawkspeed.data.network.mapper.user.UserDtoMapper
import com.vljx.hawkspeed.data.source.UserRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requestmodels.user.RequestGetUser
import javax.inject.Inject

class UserRemoteDataImpl @Inject constructor(
    private val userService: UserService,

    private val userDtoMapper: UserDtoMapper
): BaseRemoteData(), UserRemoteData {
    override suspend fun queryUserByUid(requestGetUser: RequestGetUser): Resource<UserModel> = getResult({
        userService.queryUserByUid(requestGetUser.userUid)
    }, userDtoMapper)
}