package com.vljx.hawkspeed.domain.exc.race

import com.vljx.hawkspeed.domain.ResourceError

class StartRaceFailedException(
    val socketError: ResourceError.SocketError?
): Exception()