package com.vljx.hawkspeed.data.database.mapper

import com.vljx.hawkspeed.data.database.entity.UserEntity
import com.vljx.hawkspeed.data.models.user.UserModel
import javax.inject.Inject

class UserEntityMapper @Inject constructor(

): EntityMapper<UserEntity, UserModel> {
    override fun mapFromEntity(entity: UserEntity): UserModel {
        return UserModel(
            entity.userUid,
            entity.userName,
            entity.bio,
            entity.privilege,
            entity.isBot,
            entity.isYou
        )
    }

    override fun mapToEntity(model: UserModel): UserEntity {
        return UserEntity(
            model.userUid,
            model.userName,
            model.bio,
            model.privilege,
            model.isBot,
            model.isYou
        )
    }
}