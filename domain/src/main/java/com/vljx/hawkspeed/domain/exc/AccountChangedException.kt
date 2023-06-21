package com.vljx.hawkspeed.domain.exc

class AccountChangedException: Exception("Stored account has changed vs the Account received from server! Crossed sessions???") {
    companion object {
        const val ERROR_ACCOUNT_CHANGED = "com.vljx.hawkspeed.domain.exc.AccountChangedException.ERROR_ACCOUNT_CHANGED"
    }
}