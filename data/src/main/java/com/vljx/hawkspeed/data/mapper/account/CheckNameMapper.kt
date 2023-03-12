package com.vljx.hawkspeed.data.mapper.account

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.account.CheckNameModel
import com.vljx.hawkspeed.domain.models.account.CheckName
import javax.inject.Inject

class CheckNameMapper @Inject constructor(

): Mapper<CheckNameModel, CheckName> {
    override fun mapFromData(model: CheckNameModel): CheckName {
        return CheckName(
            model.userName,
            model.isTaken
        )
    }

    override fun mapToData(domain: CheckName): CheckNameModel {
        return CheckNameModel(
            domain.userName,
            domain.isTaken
        )
    }
}