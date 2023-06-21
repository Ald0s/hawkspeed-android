package com.vljx.hawkspeed.domain.base

interface ApiErrorWrapper {
    val severity: String
    val name: String
    val errorInformation: HashMap<String, String>

    val isGlobal: Boolean
        get() = severity == "global-error"
}