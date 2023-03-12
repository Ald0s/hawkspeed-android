package com.vljx.hawkspeed.data.network.mapper.account

import com.vljx.hawkspeed.data.models.account.CheckNameModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.account.CheckNameDto
import javax.inject.Inject

class CheckNameDtoMapper @Inject constructor(

): DtoMapper<CheckNameDto, CheckNameModel> {
    override fun mapFromDto(dto: CheckNameDto): CheckNameModel {
        return CheckNameModel(
            dto.userName,
            dto.isTaken
        )
    }
}