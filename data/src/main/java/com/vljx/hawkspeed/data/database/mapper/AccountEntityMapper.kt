package com.vljx.hawkspeed.data.database.mapper

import com.vljx.hawkspeed.data.database.entity.AccountEntity
import com.vljx.hawkspeed.data.models.account.AccountModel
import javax.inject.Inject

class AccountEntityMapper @Inject constructor(

): EntityMapper<AccountEntity, AccountModel> {
    override fun mapFromEntity(entity: AccountEntity): AccountModel {
        return AccountModel(
            entity.userUid,
            entity.emailAddress,
            entity.userName,
            entity.isAccountVerified,
            entity.isPasswordVerified,
            entity.isProfileSetup,
            entity.canCreateTracks
        )
    }

    override fun mapToEntity(model: AccountModel): AccountEntity {
        return AccountEntity(
            model.userUid,
            model.emailAddress,
            model.userName,
            model.isAccountVerified,
            model.isPasswordVerified,
            model.isProfileSetup,
            model.canCreateTracks
        )
    }
}