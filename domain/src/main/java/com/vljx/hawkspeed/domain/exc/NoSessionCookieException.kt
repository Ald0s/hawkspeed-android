package com.vljx.hawkspeed.domain.exc

class NoSessionCookieException: Exception("There is no session cookie in cache!") {
    companion object {
        const val ERROR_NO_SESSION_COOKIE = "com.vljx.hawkspeed.domain.exc.NoSessionCookieException.ERROR_NO_SESSION_COOKIE"
    }
}