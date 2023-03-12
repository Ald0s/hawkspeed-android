package com.vljx.hawkspeed.domain

interface Resource<out T> {
    enum class Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    val status: Status
    val data: T?
    val resourceError: ResourceError?
}