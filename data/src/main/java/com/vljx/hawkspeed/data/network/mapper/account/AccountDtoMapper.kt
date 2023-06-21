package com.vljx.hawkspeed.data.network.mapper.account

import com.vljx.hawkspeed.data.models.account.AccountModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.account.AccountDto
import javax.inject.Inject

class AccountDtoMapper @Inject constructor(

): DtoMapper<AccountDto, AccountModel> {
    override fun mapFromDto(dto: AccountDto): AccountModel {
        return AccountModel(
            dto.userUid,
            dto.emailAddress,
            dto.userName,
            dto.isAccountVerified,
            dto.isPasswordVerified,
            dto.isProfileSetup,
            dto.canCreateTracks
        )
    }
}