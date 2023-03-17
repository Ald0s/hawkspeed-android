package com.vljx.hawkspeed.data.remotemediator

import com.vljx.hawkspeed.domain.ResourceError
import java.lang.Exception

data class RemoteMediatorResourceException(
    private val resourceError: ResourceError
): Exception()