package com.vljx.hawkspeed.data.network.mapper.user

import com.vljx.hawkspeed.data.models.user.UserModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.user.UserDto
import javax.inject.Inject

class UserDtoMapper @Inject constructor(

): DtoMapper<UserDto, UserModel> {
    override fun mapFromDto(dto: UserDto): UserModel {
        return UserModel(
            dto.userUid,
            dto.userName,
            dto.privilege,
            dto.isBot,
            dto.isYou
        )
    }
}