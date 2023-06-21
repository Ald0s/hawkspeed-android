package com.vljx.hawkspeed.domain.exc

import com.vljx.hawkspeed.domain.ResourceError

data class ResourceErrorException(
    val resourceError: ResourceError
): Exception()