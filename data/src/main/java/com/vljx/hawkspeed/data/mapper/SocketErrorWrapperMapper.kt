package com.vljx.hawkspeed.data.mapper

import com.vljx.hawkspeed.data.models.SocketErrorWrapperModel
import com.vljx.hawkspeed.domain.ResourceError
import javax.inject.Inject

class SocketErrorWrapperMapper @Inject constructor(

): Mapper<SocketErrorWrapperModel, ResourceError.SocketError> {
    override fun mapFromData(model: SocketErrorWrapperModel): ResourceError.SocketError {
        return ResourceError.SocketError(model)
    }

    override fun mapToData(domain: ResourceError.SocketError): SocketErrorWrapperModel {
        return SocketErrorWrapperModel(
            domain.socketErrorWrapper.name,
            domain.socketErrorWrapper.reason,
            domain.socketErrorWrapper.errorInformation,
        )
    }
}