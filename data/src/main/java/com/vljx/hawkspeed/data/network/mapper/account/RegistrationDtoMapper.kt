package com.vljx.hawkspeed.data.network.mapper.account

import com.vljx.hawkspeed.data.models.account.RegistrationModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.account.RegistrationDto
import javax.inject.Inject

class RegistrationDtoMapper @Inject constructor(

): DtoMapper<RegistrationDto, RegistrationModel> {
    override fun mapFromDto(dto: RegistrationDto): RegistrationModel {
        return RegistrationModel(
            dto.emailAddress
        )
    }
}