package com.vljx.hawkspeed.domain.base

interface BaseApiError {
    val severity: String
    val name: String
    val errorInformation: HashMap<String, String>
}