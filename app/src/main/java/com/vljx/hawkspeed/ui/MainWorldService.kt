package com.vljx.hawkspeed.ui

import com.vljx.hawkspeed.WorldService

interface MainWorldService {
    val isServiceBound: Boolean
    val worldService: WorldService
}