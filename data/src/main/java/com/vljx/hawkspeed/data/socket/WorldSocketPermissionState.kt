package com.vljx.hawkspeed.data.socket

import com.vljx.hawkspeed.data.socket.requests.ConnectAuthenticationRequestDto

sealed class WorldSocketPermissionState {
    data class CanJoinWorld(
        val connectAuthenticationRequestDto: ConnectAuthenticationRequestDto
    ): WorldSocketPermissionState()
    object CantJoinWorld: WorldSocketPermissionState()
}