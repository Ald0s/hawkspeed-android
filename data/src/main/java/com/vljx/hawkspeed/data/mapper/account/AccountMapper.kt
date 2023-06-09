package com.vljx.hawkspeed.data.mapper.account

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.account.AccountModel
import com.vljx.hawkspeed.domain.models.account.Account
import javax.inject.Inject

class AccountMapper @Inject constructor(

): Mapper<AccountModel, Account> {
    override fun mapFromData(model: AccountModel): Account {
        return Account(
            model.userUid,
            model.emailAddress,
            model.userName,
            model.isAccountVerified,
            model.isPasswordVerified,
            model.isProfileSetup,
            model.canCreateTracks
        )
    }

    override fun mapToData(domain: Account): AccountModel {
        return AccountModel(
            domain.userUid,
            domain.emailAddress,
            domain.userName,
            domain.isAccountVerified,
            domain.isPasswordVerified,
            domain.isProfileSetup,
            domain.canCreateTracks
        )
    }
}