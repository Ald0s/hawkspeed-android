package com.vljx.hawkspeed.view.setup

import com.vljx.hawkspeed.domain.models.account.Account

interface SetupCallback {
    fun resolveAccountIssues(account: Account)
}