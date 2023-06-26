package com.vljx.hawkspeed.domain.base

interface SocketErrorWrapper {
    val severity: String
    val name: String
    val errorInformation: HashMap<String, String>

    val isGlobal: Boolean
        get() = severity == "global-error"
}