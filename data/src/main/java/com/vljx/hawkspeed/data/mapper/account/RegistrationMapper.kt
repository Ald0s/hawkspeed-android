package com.vljx.hawkspeed.data.mapper.account

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.account.RegistrationModel
import com.vljx.hawkspeed.domain.models.account.Registration
import javax.inject.Inject

class RegistrationMapper @Inject constructor(

): Mapper<RegistrationModel, Registration> {
    override fun mapFromData(model: RegistrationModel): Registration {
        return Registration(
            model.emailAddress
        )
    }

    override fun mapToData(domain: Registration): RegistrationModel {
        return RegistrationModel(
            domain.emailAddress
        )
    }
}