package com.vljx.hawkspeed.domain

import com.vljx.hawkspeed.domain.base.BaseApiError

interface ResourceError {
    enum class Type {
        API,
        NETWORK,
        DEVICE,
        OTHER
    }

    val type: Type
    val httpStatusCode: Int?
    val httpStatus: String?
    val message: String?
    val apiError: BaseApiError?
    val exception: Exception?

    /**
     * Return a code-type prompt that represents the error as a whole; this code will be passed to all functions
     * responsible for translating errors into readable/user-friendly resources.
     */
    fun getErrorCode(): String

    /**
     * Return a printable readout that type/domain agnostically describes this resource error. This consists of multiple
     * lines each showing information only if made available by the error.
     */
    fun summariseError(): String
}