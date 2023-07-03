package com.vljx.hawkspeed.data.models

import com.vljx.hawkspeed.domain.base.SocketErrorWrapper

data class SocketErrorWrapperModel(
    override val name: String,
    override val reason: String,
    override val errorInformation: HashMap<String, String>
): SocketErrorWrapper