package com.vljx.hawkspeed.data.mapper.user

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.user.UserModel
import com.vljx.hawkspeed.domain.models.user.User
import javax.inject.Inject

class UserMapper @Inject constructor(

): Mapper<UserModel, User> {
    override fun mapFromData(model: UserModel): User {
        return User(
            model.userUid,
            model.userName,
            model.privilege,
            model.isBot,
            model.isYou
        )
    }

    override fun mapToData(domain: User): UserModel {
        return UserModel(
            domain.userUid,
            domain.userName,
            domain.privilege,
            domain.isBot,
            domain.isYou
        )
    }
}