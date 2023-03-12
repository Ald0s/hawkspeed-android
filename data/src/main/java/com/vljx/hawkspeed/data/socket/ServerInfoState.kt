package com.vljx.hawkspeed.data.socket

import com.vljx.hawkspeed.data.socket.requests.ConnectAuthenticationRequestDto

sealed class ServerInfoState {
    data class Connect(
        val entryToken: String,
        val gameServerInfo: String,
        val connectAuthenticationRequestDto: ConnectAuthenticationRequestDto
    ): ServerInfoState()

    object None: ServerInfoState()
}