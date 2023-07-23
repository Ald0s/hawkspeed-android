package com.vljx.hawkspeed.domain.exc.socket

class ConnectionBrokenException(
    reason: String = REASON_UNKNOWN
): Exception(reason) {
    companion object {
        const val REASON_SERVER_DISAPPEARED = "server-disappeared"
        const val REASON_UNKNOWN = "unknown"
    }
}